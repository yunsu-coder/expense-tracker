package com.example.expensetracker.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.expensetracker.data.db.entity.ExpenseEntity

data class DailyTotal(
    val date: Long,
    val total: Double
)

data class CategoryBreakdown(
    val categoryId: Long,
    val name: String,
    val colorHex: Long,
    val total: Double
)

data class ExpenseWithCategory(
    val id: Long,
    val amount: Double,
    val categoryId: Long,
    val note: String?,
    val date: Long,
    val createdAt: Long,
    val categoryName: String,
    val categoryColor: Long
)

@Dao
interface ExpenseDao {
    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE date BETWEEN :startDay AND :endDay")
    suspend fun getTotalForDateRange(startDay: Long, endDay: Long): Double

    @Query("SELECT COUNT(*) FROM expenses WHERE date BETWEEN :startDay AND :endDay")
    suspend fun getCountForDateRange(startDay: Long, endDay: Long): Int

    @Query("""
        SELECT date, SUM(amount) AS total FROM expenses
        WHERE date BETWEEN :startDay AND :endDay
        GROUP BY date ORDER BY date ASC
    """)
    suspend fun getDailyTotals(startDay: Long, endDay: Long): List<DailyTotal>

    @Query("""
        SELECT e.categoryId, c.name, c.colorHex, SUM(e.amount) AS total
        FROM expenses e INNER JOIN categories c ON e.categoryId = c.id
        WHERE e.date BETWEEN :startDay AND :endDay
        GROUP BY e.categoryId ORDER BY total DESC
    """)
    suspend fun getCategoryBreakdown(startDay: Long, endDay: Long): List<CategoryBreakdown>

    @Query("""
        SELECT e.id, e.amount, e.categoryId, e.note, e.date, e.createdAt,
               c.name AS categoryName, c.colorHex AS categoryColor
        FROM expenses e INNER JOIN categories c ON e.categoryId = c.id
        WHERE (:startDay IS NULL OR e.date >= :startDay)
          AND (:endDay IS NULL OR e.date <= :endDay)
          AND (:categoryId IS NULL OR e.categoryId = :categoryId)
        ORDER BY e.date DESC, e.id DESC
        LIMIT :pageSize OFFSET :offset
    """)
    suspend fun getExpensesFiltered(
        startDay: Long?, endDay: Long?, categoryId: Long?,
        pageSize: Int, offset: Int
    ): List<ExpenseWithCategory>

    @Query("""
        SELECT COUNT(*) FROM expenses e
        WHERE (:startDay IS NULL OR e.date >= :startDay)
          AND (:endDay IS NULL OR e.date <= :endDay)
          AND (:categoryId IS NULL OR e.categoryId = :categoryId)
    """)
    suspend fun getExpenseCountFiltered(
        startDay: Long?, endDay: Long?, categoryId: Long?
    ): Int

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): ExpenseEntity?

    @Delete
    suspend fun delete(expense: ExpenseEntity)
}
