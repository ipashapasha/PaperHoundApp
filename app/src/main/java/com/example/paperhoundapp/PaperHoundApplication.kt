package com.example.paperhoundapp

import android.app.Application
import com.example.paperhoundapp.data.BookRepository
import com.example.paperhoundapp.data.GoalRepository
import com.example.paperhoundapp.data.PaperHoundDatabase

class PaperHoundApplication : Application() {

    val bookRepository by lazy { BookRepository(PaperHoundDatabase.create(this).bookDao()) }

    val goalRepository by lazy { GoalRepository(this) }
}
