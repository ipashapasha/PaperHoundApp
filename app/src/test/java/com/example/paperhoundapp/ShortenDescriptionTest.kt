package com.example.paperhoundapp

import com.example.paperhoundapp.data.largeCoverUrl
import com.example.paperhoundapp.data.normalizedTitle
import com.example.paperhoundapp.data.shortenDescription
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortenDescriptionTest {

    @Test
    fun shortTextIsKept() {
        assertEquals("A desert planet.", "  A desert\n\nplanet. ".shortenDescription())
    }

    @Test
    fun longTextIsCutAtWordBoundary() {
        val text = "word ".repeat(200)
        val result = text.shortenDescription()
        assertTrue(result.length <= 400)
        assertTrue(result.endsWith("word…"))
    }

    @Test
    fun longWordIsCutHard() {
        val result = "a".repeat(1000).shortenDescription()
        assertEquals(400, result.length)
        assertTrue(result.endsWith("…"))
    }

    @Test
    fun titlesMatchIgnoringCaseAndPunctuation() {
        assertEquals("dune".normalizedTitle(), "  Dune. ".normalizedTitle())
        assertEquals("project hail mary", "Project Hail Mary (Movie Tie-In)".normalizedTitle())
        assertEquals("the hobbit or there and back again", "The Hobbit, or There and Back Again".normalizedTitle())
    }

    @Test
    fun coverUrlIsUpgraded() {
        assertEquals(
            "https://books.google.com/books/content?id=X&printsec=frontcover&img=1&zoom=1&source=gbs_api&fife=w600",
            "http://books.google.com/books/content?id=X&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs_api"
                .largeCoverUrl(),
        )
    }
}
