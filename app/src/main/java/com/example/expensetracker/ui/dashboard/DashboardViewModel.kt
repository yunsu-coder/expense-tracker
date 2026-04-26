package com.example.expensetracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.db.dao.CategoryBreakdown
import com.example.expensetracker.data.db.dao.DailyTotal
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.MonthlySummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

data class DashboardUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val summary: MonthlySummary? = null,
    val dailyTotals: List<DailyTotal> = emptyList(),
    val categoryBreakdown: List<CategoryBreakdown> = emptyList(),
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadMonth(YearMonth.now())
    }

    fun loadMonth(yearMonth: YearMonth) {
        _uiState.update { it.copy(selectedMonth = yearMonth, isLoading = true) }
        viewModelScope.launch {
            val summary = expenseRepository.getMonthlySummary(yearMonth)
            val dailyTrend = expenseRepository.getDailyTrend(yearMonth)
            val breakdown = expenseRepository.getCategoryBreakdown(yearMonth)
            _uiState.update {
                it.copy(
                    summary = summary,
                    dailyTotals = dailyTrend,
                    categoryBreakdown = breakdown,
                    isLoading = false
                )
            }
        }
    }

    fun previousMonth() {
        loadMonth(_uiState.value.selectedMonth.minusMonths(1))
    }

    fun nextMonth() {
        loadMonth(_uiState.value.selectedMonth.plusMonths(1))
    }

    class Factory(private val repo: ExpenseRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DashboardViewModel(repo) as T
    }
}
