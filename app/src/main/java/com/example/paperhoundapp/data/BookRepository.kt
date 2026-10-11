package com.example.paperhoundapp.data

import androidx.compose.runtime.mutableStateListOf
import java.time.LocalDate
import java.util.UUID

/**
 * In-memory sample data until a real storage layer is added.
 * Backed by snapshot state, so composables reading it recompose on changes.
 */
object BookRepository {

    const val YEARLY_GOAL = 12

    private val today = LocalDate.now()

    private fun daysAgo(days: Long) = today.minusDays(days)

    private val books = mutableStateListOf(
        Book("1", "Мастер и Маргарита", "Михаил Булгаков", status = ReadingStatus.Reading, addedAt = daysAgo(3)),
        Book("2", "Дюна", "Фрэнк Герберт", status = ReadingStatus.Reading, addedAt = daysAgo(12)),
        Book("3", "Пикник на обочине", "Аркадий и Борис Стругацкие", status = ReadingStatus.Reading, addedAt = daysAgo(20)),
        Book("4", "Сто лет одиночества", "Габриэль Гарсиа Маркес", addedAt = daysAgo(60), finishedAt = daysAgo(5), rating = Rating.Great),
        Book("5", "Улитка на склоне", "Аркадий и Борис Стругацкие", addedAt = daysAgo(80), finishedAt = daysAgo(18), rating = Rating.Good),
        Book("6", "Норвежский лес", "Харуки Мураками", addedAt = daysAgo(90), finishedAt = daysAgo(34), rating = Rating.Okay),
        Book("7", "Бойня номер пять", "Курт Воннегут", addedAt = daysAgo(120), finishedAt = daysAgo(51), rating = Rating.Great),
        Book("8", "Пётр Первый", "Алексей Толстой", addedAt = daysAgo(150), finishedAt = daysAgo(77), rating = Rating.Bad),
        Book("9", "Преступление и наказание", "Фёдор Достоевский", addedAt = daysAgo(400), finishedAt = daysAgo(330), rating = Rating.Good),
        Book("10", "Над пропастью во ржи", "Джером Сэлинджер", BookFormat.Audio, addedAt = daysAgo(160), finishedAt = daysAgo(12), rating = Rating.Good),
        Book("11", "Гордость и предубеждение", "Джейн Остин", BookFormat.Audio, addedAt = daysAgo(200), finishedAt = daysAgo(45), rating = Rating.Okay),
        Book("12", "Солярис", "Станислав Лем", BookFormat.Audio, addedAt = daysAgo(210), finishedAt = daysAgo(98), rating = Rating.Great),
        Book("13", "Шантарам", "Грегори Дэвид Робертс", BookFormat.Audio, addedAt = daysAgo(260), finishedAt = daysAgo(140), rating = Rating.Good),
    )

    fun shelf(shelf: Shelf): List<Book> = when (shelf) {
        Shelf.Reading -> books.filter { it.status == ReadingStatus.Reading }.sortedByDescending { it.addedAt }
        Shelf.PaperLibrary -> finished(BookFormat.Paper)
        Shelf.AudioLibrary -> finished(BookFormat.Audio)
    }

    private fun finished(format: BookFormat) = books
        .filter { it.status == ReadingStatus.Finished && it.format == format }
        .sortedByDescending { it.finishedAt }

    fun finishedInYear(year: Int): Int = books.count { it.finishedAt?.year == year }

    fun add(book: Book) {
        books.add(book)
    }

    fun newId(): String = UUID.randomUUID().toString()
}
