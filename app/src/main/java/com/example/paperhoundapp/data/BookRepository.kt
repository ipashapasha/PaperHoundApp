package com.example.paperhoundapp.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

/** Books stored in the local Room database. */
class BookRepository(private val dao: BookDao) {

    // Writes outlive the screen that started them, e.g. the add-book screen closing right after submit.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val books: Flow<List<Book>> = dao.observeAll()

    fun add(book: Book) {
        scope.launch { dao.insert(book) }
    }

    companion object {
        const val YEARLY_GOAL = 12

        fun newId(): String = UUID.randomUUID().toString()
    }
}

fun List<Book>.shelf(shelf: Shelf): List<Book> = when (shelf) {
    Shelf.Reading -> filter { it.status == ReadingStatus.Reading }.sortedByDescending { it.startedAt ?: it.addedAt }
    Shelf.PaperLibrary -> finished(BookFormat.Paper)
    Shelf.AudioLibrary -> finished(BookFormat.Audio)
}

private fun List<Book>.finished(format: BookFormat) =
    filter { it.status == ReadingStatus.Finished && it.format == format }
        .sortedByDescending { it.finishedAt }

fun List<Book>.finishedInYear(year: Int): Int = count { it.finishedAt?.year == year }
