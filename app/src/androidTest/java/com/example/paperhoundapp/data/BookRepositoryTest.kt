package com.example.paperhoundapp.data

import android.content.Context
import androidx.core.net.toUri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class BookRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val server = MockWebServer()
    private lateinit var database: PaperHoundDatabase
    private lateinit var repository: BookRepository

    @Before
    fun setUp() {
        server.start()
        database = Room.inMemoryDatabaseBuilder(context, PaperHoundDatabase::class.java).build()
        repository = BookRepository(database.bookDao(), CoverStorage(context))
    }

    @After
    fun tearDown() {
        database.close()
        server.shutdown()
        File(context.filesDir, "covers").deleteRecursively()
    }

    private suspend fun storedBook(): Book = withTimeout(5_000) { repository.books.first { it.isNotEmpty() }.single() }

    @Test
    fun addsBookWithoutCover() = runBlocking {
        val book = Book("1", "Dune", "Frank Herbert", status = ReadingStatus.Reading)
        repository.add(book)
        assertEquals(book, storedBook())
    }

    @Test
    fun keepsAPickedCoverAsIs() = runBlocking {
        val book = Book("1", "Dune", coverUri = "content://media/picker/0/42")
        repository.add(book)
        assertEquals(book, storedBook())
        assertEquals(0, server.requestCount)
    }

    @Test
    fun savesAnOnlineCoverLocally() = runBlocking {
        server.enqueue(MockResponse().setBody("jpeg"))
        val book = Book("1", "Dune", coverUri = server.url("/cover").toString())

        repository.add(book)

        val saved = File(context.filesDir, "covers/1.jpg")
        assertEquals(book.copy(coverUri = saved.toUri().toString()), storedBook())
        assertEquals("jpeg", saved.readText())
    }

    @Test
    fun keepsTheOnlineLinkWhenSavingFails() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500))
        val book = Book("1", "Dune", coverUri = server.url("/cover").toString())

        repository.add(book)

        assertEquals(book, storedBook())
    }
}
