package com.example.paperhoundapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BookTest {

    private val day = LocalDate.of(2026, 5, 10)

    @Test
    fun readingShelfShowsLatestStartedFirst() {
        val books = listOf(
            Book("old", "Old", status = ReadingStatus.Reading, addedAt = day.minusDays(30), startedAt = day.minusDays(20)),
            Book("new", "New", status = ReadingStatus.Reading, addedAt = day.minusDays(40), startedAt = day.minusDays(1)),
            // Without a start date the added date counts.
            Book("added", "Added", status = ReadingStatus.Reading, addedAt = day.minusDays(5)),
            Book("done", "Done", finishedAt = day),
        )
        assertEquals(listOf("new", "added", "old"), books.shelf(Shelf.Reading).map { it.id })
    }

    @Test
    fun librariesShowFinishedBooksOfTheirFormatLatestFirst() {
        val books = listOf(
            Book("p1", "Paper 1", finishedAt = day.minusDays(10)),
            Book("p2", "Paper 2", finishedAt = day),
            Book("a1", "Audio 1", format = BookFormat.Audio, finishedAt = day.minusDays(3)),
            Book("a2", "Audio 2", format = BookFormat.Audio, finishedAt = day.minusDays(1)),
            Book("r", "Reading audio", format = BookFormat.Audio, status = ReadingStatus.Reading),
        )
        assertEquals(listOf("p2", "p1"), books.shelf(Shelf.PaperLibrary).map { it.id })
        assertEquals(listOf("a2", "a1"), books.shelf(Shelf.AudioLibrary).map { it.id })
    }

    @Test
    fun emptyListGivesEmptyShelves() {
        Shelf.entries.forEach { assertTrue(emptyList<Book>().shelf(it).isEmpty()) }
    }

    @Test
    fun bookDefaults() {
        val book = Book("1", "Title")
        assertNull(book.author)
        assertEquals(BookFormat.Paper, book.format)
        assertEquals(ReadingStatus.Finished, book.status)
        assertEquals(LocalDate.now(), book.addedAt)
        assertNull(book.coverUri)
        assertNull(book.rating)
    }

    @Test
    fun ratingsGoFromWorstToBest() {
        assertEquals(listOf("😞", "🙁", "😐", "🙂", "😄"), Rating.entries.map { it.emoji })
        assertEquals(Rating.entries.size, Rating.entries.map { it.labelRes }.toSet().size)
    }

    @Test
    fun newIdsAreUnique() {
        assertNotEquals(BookRepository.newId(), BookRepository.newId())
    }

    @Test
    fun remoteUrls() {
        assertTrue("https://books.google.com/cover.jpg".isRemoteUrl())
        assertTrue("http://books.google.com/cover.jpg".isRemoteUrl())
        assertFalse("content://media/picker/0/1".isRemoteUrl())
        assertFalse("file:///data/covers/1.jpg".isRemoteUrl())
        assertFalse("".isRemoteUrl())
    }

    @Test
    fun datesAreStoredAsEpochDays() {
        val converters = Converters()
        assertEquals(day.toEpochDay(), converters.fromLocalDate(day))
        assertEquals(day, converters.toLocalDate(day.toEpochDay()))
        assertNull(converters.fromLocalDate(null))
        assertNull(converters.toLocalDate(null))
    }

    @Test
    fun sampleBooksFillEveryShelf() {
        Shelf.entries.forEach { assertTrue(SampleBooks.all.shelf(it).isNotEmpty()) }
        assertEquals(SampleBooks.all.size, SampleBooks.all.map { it.id }.toSet().size)
    }
}
