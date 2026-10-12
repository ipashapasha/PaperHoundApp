package com.example.paperhoundapp.data

import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.paperhoundapp.R
import java.time.LocalDate

enum class ReadingStatus { Reading, Finished }

enum class BookFormat { Paper, Audio }

enum class Rating(val emoji: String, @StringRes val labelRes: Int) {
    Awful("😞", R.string.rating_awful),
    Bad("🙁", R.string.rating_bad),
    Okay("😐", R.string.rating_okay),
    Good("🙂", R.string.rating_good),
    Great("😄", R.string.rating_great),
}

@Entity(tableName = "books")
data class Book(
    @PrimaryKey val id: String,
    val title: String,
    val author: String? = null,
    val format: BookFormat = BookFormat.Paper,
    val status: ReadingStatus = ReadingStatus.Finished,
    val addedAt: LocalDate = LocalDate.now(),
    /** Optional for books that are still being read. */
    val startedAt: LocalDate? = null,
    val finishedAt: LocalDate? = null,
    val isbn: String? = null,
    val link: String? = null,
    /** Content URI of a cover picked from the device. */
    val coverUri: String? = null,
    val rating: Rating? = null,
)

/** A section of the home screen that has its own full-list page. */
enum class Shelf(@StringRes val titleRes: Int) {
    Reading(R.string.shelf_reading),
    PaperLibrary(R.string.shelf_paper_library),
    AudioLibrary(R.string.shelf_audio_library),
}
