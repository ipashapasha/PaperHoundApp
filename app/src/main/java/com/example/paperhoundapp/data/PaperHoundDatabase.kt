package com.example.paperhoundapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDate

@Database(entities = [Book::class], version = 1)
@TypeConverters(Converters::class)
abstract class PaperHoundDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao

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
