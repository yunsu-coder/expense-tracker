package com.example.expensetracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expensetracker.data.db.converter.Converters
import com.example.expensetracker.data.db.dao.CategoryDao
import com.example.expensetracker.data.db.dao.ExpenseDao
import com.example.expensetracker.data.db.entity.CategoryEntity
import com.example.expensetracker.data.db.entity.ExpenseEntity
import com.example.expensetracker.util.DB_NAME
import com.example.expensetracker.util.SEED_CATEGORIES
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [ExpenseEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, DB_NAME
                )
                    .addCallback(SeedCallback())
                    .build()
                    .also { INSTANCE = it }
            }
    }

    private class SeedCallback : Callback() {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch {
                INSTANCE?.categoryDao()?.insertAll(SEED_CATEGORIES)
            }
        }
    }
}
