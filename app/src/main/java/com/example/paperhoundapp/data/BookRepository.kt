package com.example.paperhoundapp.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

/** Books stored in the local Room database. */
class BookRepository(private val dao: BookDao, private val covers: CoverStorage) {

    // Writes outlive the screen that started them, e.g. the add-book screen closing right after submit.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val books: Flow<List<Book>> = dao.observeAll()

    fun add(book: Book) {
        scope.launch {
            // A cover found online is saved locally; if that fails the link is kept and loaded from the network.
            val savedCover = book.coverUri?.takeIf { it.isRemoteUrl() }?.let { covers.save(book.id, it) }
            dao.insert(if (savedCover != null) book.copy(coverUri = savedCover) else book)
        }
    }

    companion object {
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
