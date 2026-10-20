package com.example.paperhoundapp.data

import android.content.Context
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class CoverStorageTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val server = MockWebServer()
    private val storage = CoverStorage(context)

    @Before
    fun setUp() {
        File(context.filesDir, "covers").deleteRecursively()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
        File(context.filesDir, "covers").deleteRecursively()
    }

    @Test
    fun savesTheDownloadedCover() {
        val image = byteArrayOf(1, 2, 3, 4, 5)
        server.enqueue(MockResponse().setBody(Buffer().write(image)))

        val uri = storage.save("book-1", server.url("/cover.jpg").toString())

        val file = File(context.filesDir, "covers/book-1.jpg")
        assertEquals(file.toUri().toString(), uri)
        assertArrayEquals(image, file.readBytes())
        assertEquals("/cover.jpg", server.takeRequest().path)
    }

    @Test
    fun returnsNullWhenTheServerRefuses() {
        server.enqueue(MockResponse().setResponseCode(404))

        assertNull(storage.save("book-1", server.url("/cover.jpg").toString()))
        assertEquals(false, File(context.filesDir, "covers/book-1.jpg").exists())
    }

    @Test
    fun returnsNullWhenTheServerIsUnreachable() {
        val url = server.url("/cover.jpg").toString()
        server.shutdown()

        assertNull(storage.save("book-1", url))
    }
}
