package com.example.expensetracker.data.repository

import com.example.expensetracker.data.db.dao.CategoryBreakdown
import com.example.expensetracker.data.db.dao.DailyTotal
import com.example.expensetracker.data.db.dao.ExpenseDao
import com.example.expensetracker.data.db.dao.ExpenseWithCategory
import com.example.expensetracker.data.db.entity.ExpenseEntity
import com.example.expensetracker.util.PAGE_SIZE
import java.time.LocalDate
import java.time.YearMonth

data class MonthlySummary(
    val totalExpense: Double,
    val expenseCount: Int,
    val avgPerDay: Double
)

data class PagedExpenses(
    val items: List<ExpenseWithCategory>,
    val hasMore: Boolean,
    val totalCount: Int
)

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    suspend fun getMonthlySummary(yearMonth: YearMonth): MonthlySummary {
        val start = yearMonth.atDay(1).toEpochDay()
        val end = yearMonth.atEndOfMonth().toEpochDay()
        val total = expenseDao.getTotalForDateRange(start, end)
        val count = expenseDao.getCountForDateRange(start, end)
        val days = yearMonth.lengthOfMonth()
        return MonthlySummary(
            totalExpense = total,
            expenseCount = count,
            avgPerDay = if (days > 0) total / days else 0.0
        )
    }

    suspend fun getDailyTrend(yearMonth: YearMonth): List<DailyTotal> {
        val start = yearMonth.atDay(1).toEpochDay()
        val end = yearMonth.atEndOfMonth().toEpochDay()
        return expenseDao.getDailyTotals(start, end)
    }

    suspend fun getCategoryBreakdown(yearMonth: YearMonth): List<CategoryBreakdown> {
        val start = yearMonth.atDay(1).toEpochDay()
        val end = yearMonth.atEndOfMonth().toEpochDay()
        return expenseDao.getCategoryBreakdown(start, end)
    }

    suspend fun getExpenses(
        startDate: LocalDate?,
        endDate: LocalDate?,
        categoryId: Long?,
        page: Int
    ): PagedExpenses {
        val startDay = startDate?.toEpochDay()
        val endDay = endDate?.toEpochDay()
        val offset = page * PAGE_SIZE
        val items = expenseDao.getExpensesFiltered(startDay, endDay, categoryId, PAGE_SIZE, offset)
        val totalCount = expenseDao.getExpenseCountFiltered(startDay, endDay, categoryId)
        return PagedExpenses(
            items = items,
            hasMore = (offset + items.size) < totalCount,
            totalCount = totalCount
        )
    }

    suspend fun getById(id: Long): ExpenseEntity? = expenseDao.getById(id)

    suspend fun save(expense: ExpenseEntity): Long = expenseDao.insert(expense)

    suspend fun update(expense: ExpenseEntity) = expenseDao.update(expense)

    suspend fun delete(expense: ExpenseEntity) = expenseDao.delete(expense)
}
