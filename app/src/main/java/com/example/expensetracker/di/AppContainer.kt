package com.example.expensetracker.di

import android.app.Application
import com.example.expensetracker.data.db.AppDatabase
import com.example.expensetracker.data.repository.CategoryRepository
import com.example.expensetracker.data.repository.ExpenseRepository

class AppContainer(application: Application) {
    private val database by lazy { AppDatabase.getInstance(application) }
    val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    val expenseRepository by lazy { ExpenseRepository(database.expenseDao()) }
}
