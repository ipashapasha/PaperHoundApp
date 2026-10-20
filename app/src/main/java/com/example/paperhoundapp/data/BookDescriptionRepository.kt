package com.example.paperhoundapp.data

import android.text.Html
import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/** A book description shortened to [MAX_DESCRIPTION_LENGTH] with a link to the full text, plus the cover. */
data class BookDescription(val text: String, val sourceUrl: String?, val author: String?, val coverUrl: String? = null)

const val MAX_DESCRIPTION_LENGTH = 400

/** Looks up English book descriptions in the Google Books API; the URL and retry budget are swapped in tests. */
class BookDescriptionRepository(
    private val apiKey: String,
    private val volumesUrl: String = VOLUMES_URL,
    private val retryBudgetMillis: Long = RETRY_BUDGET_MILLIS,
) {

    // Successful answers by query, so searching the same title again doesn't wait on the flaky API.
    private val cache = ConcurrentHashMap<String, List<BookDescription>>()

    /**
     * Descriptions of books with this title, one per author, most relevant first; empty when none is found.
     * @throws IOException when Google Books can't be reached or keeps failing.
     */
    suspend fun find(title: String): List<BookDescription> = withContext(Dispatchers.IO) {
        cache[title]?.let { return@withContext it }
        try {
            // A plain query ranks non-fiction about the words first ("Dune" brings up sand dune studies,
            // "1984" yearbooks), so ask with a fiction hint first; it is only a ranking hint,
            // non-fiction like "Sapiens" still comes back. Fall back to the plain query if nothing matches.
            val results = parse(requestWithRetry("$title subject:fiction"), title)
                .ifEmpty { parse(requestWithRetry(title), title) }
            results.also { cache[title] = it }
        } catch (e: JSONException) {
            throw IOException("Unexpected Google Books response", e)
        } catch (e: IOException) {
            Log.w(TAG, "Search for \"$title\" failed", e)
            throw e
        }
    }

    // Google Books often fails most requests with 503 backendFailed for a while, independent of the query,
    // and any single retry may get through, so keep retrying quickly until the time budget runs out.
    private suspend fun requestWithRetry(query: String): String {
        val deadline = System.currentTimeMillis() + retryBudgetMillis
        var attempt = 0
        while (true) {
            try {
                return request(query)
            } catch (e: ServerBusyException) {
                Log.w(TAG, "Attempt ${attempt + 1} for \"$query\" failed: ${e.message}")
                attempt++
                val pause = (RETRY_DELAY_MILLIS * attempt).coerceAtMost(MAX_RETRY_DELAY_MILLIS)
                if (System.currentTimeMillis() + pause >= deadline) throw e
                delay(pause)
            }
        }
    }

    private fun request(query: String): String {
        val url = volumesUrl.toUri().buildUpon()
            // Not intitle:, which currently returns no results at all.
            .appendQueryParameter("q", query)
            .appendQueryParameter("langRestrict", "en")
            .appendQueryParameter("printType", "books")
            .appendQueryParameter("maxResults", "40")
            .appendQueryParameter("fields", "items(volumeInfo(title,authors,description,language,infoLink,canonicalVolumeLink,imageLinks/thumbnail))")
            .apply { if (apiKey.isNotEmpty()) appendQueryParameter("key", apiKey) }
            .build()
            .toString()
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MILLIS
            connection.readTimeout = TIMEOUT_MILLIS
            if (connection.responseCode >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
                throw ServerBusyException(connection.responseCode)
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("Google Books responded with ${connection.responseCode}")
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun parse(body: String, title: String): List<BookDescription> {
        val items = JSONObject(body).optJSONArray("items") ?: return emptyList()
        val wanted = title.normalizedTitle()
        // Editions of the same book repeat, so keep the most relevant one per author.
        val byAuthor = LinkedHashMap<String, BookDescription>()
        // A plain search also matches descriptions and authors, so only a book with the same title counts.
        for (i in 0 until items.length()) {
            val info = items.getJSONObject(i).optJSONObject("volumeInfo") ?: continue
            if (info.optString("language", "en") != "en") continue
            if (info.optString("title").normalizedTitle() != wanted) continue
            val description = info.optString("description").takeIf { it.isNotBlank() } ?: continue
            val text = Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT).toString()
            val sourceUrl = info.optString("canonicalVolumeLink").ifEmpty { info.optString("infoLink") }
            val author = info.optJSONArray("authors")?.optString(0)?.takeIf { it.isNotBlank() }
            val coverUrl = info.optJSONObject("imageLinks")?.optString("thumbnail")?.takeIf { it.isNotEmpty() }
            byAuthor.getOrPut(author?.normalizedTitle().orEmpty()) {
                BookDescription(text.shortenDescription(), sourceUrl.ifEmpty { null }, author, coverUrl?.largeCoverUrl())
            }
        }
        return byAuthor.values.toList()
    }

    private companion object {
        const val TAG = "BookDescriptions"
        const val VOLUMES_URL = "https://www.googleapis.com/books/v1/volumes"
        const val TIMEOUT_MILLIS = 10_000
        const val RETRY_BUDGET_MILLIS = 15_000L
        const val RETRY_DELAY_MILLIS = 250L
        const val MAX_RETRY_DELAY_MILLIS = 1_000L
    }
}

private class ServerBusyException(code: Int) : IOException("Google Books responded with $code")

/** Lowercase words without edition notes, so "Dune." and "Dune (Movie Tie-In)" both match "dune". */
internal fun String.normalizedTitle(): String =
    replace(Regex("\\(.*?\\)|\\[.*?]"), " ")
        .lowercase()
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
        .trim()

/**
 * Google returns a 128 px wide thumbnail over http with a page-curl effect;
 * the same image is served over https, without the curl and 600 px wide when asked with `fife`.
 */
internal fun String.largeCoverUrl(): String =
    replaceFirst("http://", "https://").replace("&edge=curl", "") + "&fife=w600"

/** Collapses whitespace and cuts the text at a word boundary so it fits [maxLength], ellipsis included. */
fun String.shortenDescription(maxLength: Int = MAX_DESCRIPTION_LENGTH): String {
    val text = trim().replace(Regex("\\s+"), " ")
    if (text.length <= maxLength) return text
    val cut = text.substring(0, maxLength - 1)
    val lastSpace = cut.lastIndexOf(' ')
    val end = if (lastSpace > maxLength / 2) lastSpace else cut.length
    return cut.substring(0, end).trimEnd(' ', ',', ';', ':', '.', '-') + "…"
}
