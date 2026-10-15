package com.example.paperhoundapp.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import java.time.LocalDate

@Database(
    entities = [Book::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2, spec = PaperHoundDatabase.DropIsbnAndLink::class)],
)
@TypeConverters(Converters::class)
abstract class PaperHoundDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao

    @DeleteColumn(tableName = "books", columnName = "isbn")
    @DeleteColumn(tableName = "books", columnName = "link")
    class DropIsbnAndLink : AutoMigrationSpec

    companion object {
        fun create(context: Context): PaperHoundDatabase =
            Room.databaseBuilder(context, PaperHoundDatabase::class.java, "paperhound.db").build()
    }
}

/** Dates are stored as epoch days. Enums use Room's built-in name mapping. */
class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)
}
