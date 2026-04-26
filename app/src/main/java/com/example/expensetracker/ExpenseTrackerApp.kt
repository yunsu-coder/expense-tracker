package com.example.expensetracker

import android.app.Application
import com.example.expensetracker.di.AppContainer

class ExpenseTrackerApp : Application() {
    val container by lazy { AppContainer(this) }
}
