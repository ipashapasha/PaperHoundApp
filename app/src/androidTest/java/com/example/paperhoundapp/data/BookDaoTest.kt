package com.example.paperhoundapp.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class BookDaoTest {

    private lateinit var database: PaperHoundDatabase
    private lateinit var dao: BookDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), PaperHoundDatabase::class.java)
            .build()
        dao = database.bookDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun startsEmpty() = runTest {
        assertEquals(emptyList<Book>(), dao.observeAll().first())
    }

    @Test
    fun storesEveryField() = runTest {
        val book = Book(
            id = "1",
            title = "Dune",
            author = "Frank Herbert",
            format = BookFormat.Audio,
            status = ReadingStatus.Finished,
            addedAt = LocalDate.of(2026, 1, 2),
            startedAt = LocalDate.of(2026, 1, 3),
            finishedAt = LocalDate.of(2026, 2, 4),
            coverUri = "file:///covers/1.jpg",
            rating = Rating.Great,
        )
        dao.insert(book)
        assertEquals(listOf(book), dao.observeAll().first())
    }

    @Test
    fun storesOptionalFieldsAsNull() = runTest {
        val book = Book("1", "Dune", status = ReadingStatus.Reading)
        dao.insert(book)
        assertEquals(listOf(book), dao.observeAll().first())
    }

    @Test
    fun insertingTheSameIdReplacesTheBook() = runTest {
        dao.insert(Book("1", "Dune", status = ReadingStatus.Reading))
        dao.insert(Book("2", "Solaris"))
        val finished = Book("1", "Dune", finishedAt = LocalDate.of(2026, 3, 1), rating = Rating.Good)
        dao.insert(finished)
        assertEquals(setOf(finished, Book("2", "Solaris")), dao.observeAll().first().toSet())
    }

    @Test
    fun observersSeeNewBooks() = runTest {
        dao.insert(Book("1", "Dune"))
        dao.insert(Book("2", "Solaris"))
        assertEquals(setOf("1", "2"), dao.observeAll().first { it.size == 2 }.map { it.id }.toSet())
    }
}
