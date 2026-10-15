package com.example.paperhoundapp.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Keeps copies of downloaded covers in app storage, so they don't depend on the remote link staying alive. */
class CoverStorage(context: Context) {

    private val directory = File(context.filesDir, "covers")

    /** Returns a file URI of the saved copy, or null when the download fails. Call off the main thread. */
    fun save(bookId: String, url: String): String? {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = TIMEOUT_MILLIS
            connection.readTimeout = TIMEOUT_MILLIS
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            directory.mkdirs()
            val file = File(directory, "$bookId.jpg")
            connection.inputStream.use { input -> file.outputStream().use(input::copyTo) }
            Uri.fromFile(file).toString()
        } catch (e: IOException) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val TIMEOUT_MILLIS = 10_000
    }
}

fun String.isRemoteUrl(): Boolean = startsWith("https://") || startsWith("http://")
