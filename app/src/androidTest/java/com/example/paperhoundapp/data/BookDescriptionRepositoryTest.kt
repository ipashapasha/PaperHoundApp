package com.example.paperhoundapp.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class BookDescriptionRepositoryTest {

    private val server = MockWebServer()

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun repository(apiKey: String = "", retryBudgetMillis: Long = 2_000) =
        BookDescriptionRepository(apiKey, server.url("/books/v1/volumes").toString(), retryBudgetMillis)

    private fun volume(
        title: String,
        description: String? = "A desert planet.",
        author: String? = "Frank Herbert",
        language: String? = null,
        canonicalLink: String? = null,
        infoLink: String? = null,
        thumbnail: String? = null,
    ) = JSONObject().put(
        "volumeInfo",
        JSONObject().apply {
            put("title", title)
            description?.let { put("description", it) }
            author?.let { put("authors", JSONArray().put(it)) }
            language?.let { put("language", it) }
            canonicalLink?.let { put("canonicalVolumeLink", it) }
            infoLink?.let { put("infoLink", it) }
            thumbnail?.let { put("imageLinks", JSONObject().put("thumbnail", it)) }
        },
    )

    private fun answer(vararg volumes: JSONObject) =
        MockResponse().setBody(JSONObject().put("items", JSONArray(volumes.toList())).toString())

    private fun find(title: String, repository: BookDescriptionRepository = repository()) =
        runBlocking { repository.find(title) }

    @Test
    fun findsTheDescriptionWithLinkAuthorAndCover() {
        server.enqueue(
            answer(
                volume(
                    title = "Dune",
                    description = "<p>Set on the <b>desert</b> planet Arrakis &amp; beyond.</p>",
                    canonicalLink = "https://books.google.com/books/about/Dune.html",
                    infoLink = "https://books.google.com/info",
                    thumbnail = "http://books.google.com/books/content?id=X&edge=curl&zoom=1",
                ),
            ),
        )

        val results = find("Dune")

        assertEquals(
            listOf(
                BookDescription(
                    text = "Set on the desert planet Arrakis & beyond.",
                    sourceUrl = "https://books.google.com/books/about/Dune.html",
                    author = "Frank Herbert",
                    coverUrl = "https://books.google.com/books/content?id=X&zoom=1&fife=w600",
                ),
            ),
            results,
        )
    }

    @Test
    fun asksForEnglishFictionBooks() {
        server.enqueue(answer(volume("Dune")))

        find("Dune")

        val url = server.takeRequest().requestUrl!!
        assertEquals("/books/v1/volumes", url.encodedPath)
        assertEquals("Dune subject:fiction", url.queryParameter("q"))
        assertEquals("en", url.queryParameter("langRestrict"))
        assertEquals("books", url.queryParameter("printType"))
        assertEquals("40", url.queryParameter("maxResults"))
        assertNull(url.queryParameter("key"))
    }

    @Test
    fun sendsTheApiKeyWhenThereIsOne() {
        server.enqueue(answer(volume("Dune")))

        find("Dune", repository(apiKey = "secret"))

        assertEquals("secret", server.takeRequest().requestUrl!!.queryParameter("key"))
    }

    @Test
    fun fallsBackToThePlainQueryWhenFictionFindsNothing() {
        server.enqueue(MockResponse().setBody("{}"))
        server.enqueue(answer(volume("Sapiens", author = "Yuval Noah Harari")))

        val results = find("Sapiens")

        assertEquals(listOf("Yuval Noah Harari"), results.map { it.author })
        assertEquals("Sapiens subject:fiction", server.takeRequest().requestUrl!!.queryParameter("q"))
        assertEquals("Sapiens", server.takeRequest().requestUrl!!.queryParameter("q"))
    }

    @Test
    fun returnsNothingWhenNoBookMatches() {
        server.enqueue(MockResponse().setBody("{}"))
        server.enqueue(answer(volume("Dune Messiah")))

        assertEquals(emptyList<BookDescription>(), find("Dune"))
    }

    @Test
    fun skipsOtherTitlesLanguagesAndMissingDescriptions() {
        server.enqueue(
            answer(
                JSONObject(),
                volume("Dune Messiah", author = "A"),
                volume("Dune", author = "B", language = "de"),
                volume("Dune", author = "C", description = null),
                volume("Dune", author = "D", description = "   "),
                volume("Dune (Movie Tie-In)", author = "E", description = "Matches."),
            ),
        )

        assertEquals(listOf("E"), find("dune").map { it.author })
    }

    @Test
    fun keepsTheMostRelevantEditionPerAuthor() {
        server.enqueue(
            answer(
                volume("Solaris", author = "Stanisław Lem", description = "First edition."),
                volume("Solaris", author = "Chandler Davis", description = "Another book."),
                volume("Solaris", author = "Stanisław Lem", description = "Second edition."),
                volume("Solaris", author = null, description = "No author."),
            ),
        )

        val results = find("Solaris")

        assertEquals(listOf("First edition.", "Another book.", "No author."), results.map { it.text })
        assertNull(results.last().author)
    }

    @Test
    fun fallsBackToTheInfoLinkAndMissingCover() {
        server.enqueue(answer(volume("Dune", infoLink = "https://books.google.com/info")))

        val result = find("Dune").single()

        assertEquals("https://books.google.com/info", result.sourceUrl)
        assertNull(result.coverUrl)
    }

    @Test
    fun hasNoLinkWhenGoogleGivesNone() {
        server.enqueue(answer(volume("Dune")))

        assertNull(find("Dune").single().sourceUrl)
    }

    @Test
    fun shortensLongDescriptions() {
        server.enqueue(answer(volume("Dune", description = "word ".repeat(300))))

        val text = find("Dune").single().text
        assertTrue(text.length <= MAX_DESCRIPTION_LENGTH)
        assertTrue(text.endsWith("…"))
    }

    @Test
    fun remembersSuccessfulAnswers() {
        server.enqueue(answer(volume("Dune")))
        val repository = repository()

        val first = find("Dune", repository)
        val second = find("Dune", repository)

        assertEquals(first, second)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun retriesWhileGoogleIsBusy() {
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(answer(volume("Dune")))

        assertEquals(1, find("Dune").size)
        assertEquals(3, server.requestCount)
    }

    @Test
    fun givesUpWhenGoogleStaysBusy() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest) = MockResponse().setResponseCode(503)
        }

        val error = assertThrows(IOException::class.java) { find("Dune", repository(retryBudgetMillis = 1_000)) }

        assertEquals("Google Books responded with 503", error.message)
        assertTrue(server.requestCount > 1)
    }

    @Test
    fun failsRightAwayOnClientErrors() {
        server.enqueue(MockResponse().setResponseCode(403))

        val error = assertThrows(IOException::class.java) { find("Dune") }

        assertEquals("Google Books responded with 403", error.message)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun reportsBrokenAnswersAsIoErrors() {
        server.enqueue(MockResponse().setBody("<html>not json</html>"))

        val error = assertThrows(IOException::class.java) { find("Dune") }

        assertEquals("Unexpected Google Books response", error.message)
    }

    @Test
    fun failedSearchesAreNotRemembered() {
        server.enqueue(MockResponse().setResponseCode(404))
        server.enqueue(answer(volume("Dune")))
        val repository = repository()

        assertThrows(IOException::class.java) { find("Dune", repository) }
        assertEquals(1, find("Dune", repository).size)
    }

    @Test
    fun failsWhenGoogleIsUnreachable() {
        val repository = repository()
        server.shutdown()

        assertThrows(IOException::class.java) { find("Dune", repository) }
    }
}
