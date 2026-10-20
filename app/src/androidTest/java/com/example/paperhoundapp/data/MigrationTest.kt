package com.example.paperhoundapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/** Opens a version 1 database the way the first release created it and lets Room migrate it. */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    @After
    fun deleteDatabase() {
        context.deleteDatabase(DB_NAME)
    }

    @Test
    fun migrationFrom1To2DropsIsbnAndLinkAndKeepsBooks(): Unit = runBlocking {
        val added = LocalDate.of(2025, 6, 1)
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DB_NAME), null).use { db ->
            // From schemas/.../1.json.
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `books` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `author` TEXT, " +
                    "`format` TEXT NOT NULL, `status` TEXT NOT NULL, `addedAt` INTEGER NOT NULL, `startedAt` INTEGER, " +
                    "`finishedAt` INTEGER, `isbn` TEXT, `link` TEXT, `coverUri` TEXT, `rating` TEXT, PRIMARY KEY(`id`))",
            )
            db.execSQL(
                "INSERT INTO books (id, title, author, format, status, addedAt, isbn, link, coverUri, rating) " +
                    "VALUES ('1', 'Dune', 'Frank Herbert', 'Paper', 'Finished', ${added.toEpochDay()}, " +
                    "'9780441013593', 'https://example.com', NULL, 'Great')",
            )
            db.version = 1
        }

        val database = Room.databaseBuilder(context, PaperHoundDatabase::class.java, DB_NAME).build()
        try {
            assertEquals(
                listOf(Book("1", "Dune", "Frank Herbert", addedAt = added, rating = Rating.Great)),
                database.bookDao().observeAll().first(),
            )
            val columns = database.openHelper.readableDatabase.query("PRAGMA table_info(books)").use { cursor ->
                buildList { while (cursor.moveToNext()) add(cursor.getString(cursor.getColumnIndexOrThrow("name"))) }
            }
            assertEquals(false, "isbn" in columns || "link" in columns)
        } finally {
            database.close()
        }
    }

    private companion object {
        const val DB_NAME = "migration-test.db"
    }
}
