package com.example.paperhoundapp

import android.app.Application
import com.example.paperhoundapp.data.BookDescriptionRepository
import com.example.paperhoundapp.data.BookRepository
import com.example.paperhoundapp.data.CoverStorage
import com.example.paperhoundapp.data.GoalRepository
import com.example.paperhoundapp.data.PaperHoundDatabase

class PaperHoundApplication : Application() {

    val bookRepository by lazy { BookRepository(PaperHoundDatabase.create(this).bookDao(), CoverStorage(this)) }

    val goalRepository by lazy { GoalRepository(this) }

    val descriptionRepository by lazy { BookDescriptionRepository(BuildConfig.GOOGLE_BOOKS_API_KEY) }
}
