package com.example.expensetracker.ui.expenselist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.db.dao.ExpenseWithCategory
import com.example.expensetracker.data.db.entity.ExpenseEntity
import com.example.expensetracker.data.repository.CategoryRepository
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.PagedExpenses
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class ExpenseListUiState(
    val expenses: List<ExpenseWithCategory> = emptyList(),
    val isLoading: Boolean = false,
    val currentPage: Int = 0,
    val hasMore: Boolean = false,
    val totalCount: Int = 0,
    val selectedCategoryId: Long? = null,
    val startDate: LocalDate = YearMonth.now().atDay(1),
    val endDate: LocalDate = YearMonth.now().atEndOfMonth(),
    val showDatePicker: Boolean = false,
    val isStartDate: Boolean = true,
    val deleteTarget: ExpenseWithCategory? = null
)

class ExpenseListViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseListUiState())
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()

    val categories = categoryRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadExpenses()
    }

    fun loadExpenses() {
        _uiState.update { it.copy(isLoading = true, currentPage = 0, expenses = emptyList()) }
        viewModelScope.launch {
            val state = _uiState.value
            val result = expenseRepository.getExpenses(
                state.startDate, state.endDate, state.selectedCategoryId, 0
            )
            _uiState.update {
                it.copy(
                    expenses = result.items,
                    isLoading = false,
                    currentPage = 0,
                    hasMore = result.hasMore,
                    totalCount = result.totalCount
                )
            }
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || !state.hasMore) return
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val state = _uiState.value
            val nextPage = state.currentPage + 1
            val result = expenseRepository.getExpenses(
                state.startDate, state.endDate, state.selectedCategoryId, nextPage
            )
            _uiState.update {
                it.copy(
                    expenses = it.expenses + result.items,
                    isLoading = false,
                    currentPage = nextPage,
                    hasMore = result.hasMore
                )
            }
        }
    }

    fun selectCategory(categoryId: Long?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        loadExpenses()
    }

    fun setDateRange(start: LocalDate, end: LocalDate) {
        _uiState.update { it.copy(startDate = start, endDate = end) }
        loadExpenses()
    }

    fun requestDelete(expense: ExpenseWithCategory) =
        _uiState.update { it.copy(deleteTarget = expense) }

    fun dismissDelete() = _uiState.update { it.copy(deleteTarget = null) }

    fun confirmDelete() {
        val target = _uiState.value.deleteTarget ?: return
        viewModelScope.launch {
            expenseRepository.delete(
                ExpenseEntity(
                    id = target.id,
                    amount = target.amount,
                    categoryId = target.categoryId,
                    date = java.time.LocalDate.ofEpochDay(target.date)
                )
            )
            _uiState.update { it.copy(deleteTarget = null) }
            loadExpenses()
        }
    }

    class Factory(
        private val expenseRepo: ExpenseRepository,
        private val categoryRepo: CategoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ExpenseListViewModel(expenseRepo, categoryRepo) as T
    }
}
