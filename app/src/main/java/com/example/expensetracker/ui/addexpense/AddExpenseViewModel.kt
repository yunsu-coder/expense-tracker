package com.example.expensetracker.ui.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.db.entity.ExpenseEntity
import com.example.expensetracker.data.repository.CategoryRepository
import com.example.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddExpenseUiState(
    val amountText: String = "",
    val selectedCategoryId: Long? = null,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val isEditing: Boolean = false,
    val editingId: Long? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val amountError: Boolean = false,
    val categoryError: Boolean = false,
    val loaded: Boolean = false
)

class AddExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val expenseId: Long? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    val categories = categoryRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        if (expenseId != null) {
            loadExpense(expenseId)
        } else {
            _uiState.update { it.copy(loaded = true) }
        }
    }

    private fun loadExpense(id: Long) {
        viewModelScope.launch {
            val entity = expenseRepository.getById(id)
            if (entity != null) {
                _uiState.update {
                    it.copy(
                        amountText = if (entity.amount == entity.amount.toLong().toDouble())
                            entity.amount.toLong().toString() else entity.amount.toString(),
                        selectedCategoryId = entity.categoryId,
                        date = entity.date,
                        note = entity.note ?: "",
                        isEditing = true,
                        editingId = entity.id,
                        loaded = true
                    )
                }
            } else {
                _uiState.update { it.copy(loaded = true) }
            }
        }
    }

    fun onAmountChange(text: String) =
        _uiState.update { it.copy(amountText = text, amountError = false) }

    fun onCategorySelect(id: Long) =
        _uiState.update { it.copy(selectedCategoryId = id, categoryError = false) }

    fun onDateChange(date: LocalDate) =
        _uiState.update { it.copy(date = date) }

    fun onNoteChange(note: String) =
        _uiState.update { it.copy(note = note) }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return
        val amount = state.amountText.toDoubleOrNull()

        var hasError = false
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(amountError = true) }
            hasError = true
        }
        if (state.selectedCategoryId == null) {
            _uiState.update { it.copy(categoryError = true) }
            hasError = true
        }
        if (hasError) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val entity = ExpenseEntity(
                    id = state.editingId ?: 0,
                    amount = amount!!,
                    categoryId = state.selectedCategoryId!!,
                    date = state.date,
                    note = state.note.ifBlank { null }
                )
                if (state.isEditing) {
                    expenseRepository.update(entity)
                } else {
                    expenseRepository.save(entity)
                }
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    class Factory(
        private val expenseRepo: ExpenseRepository,
        private val categoryRepo: CategoryRepository,
        private val expenseId: Long? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AddExpenseViewModel(expenseRepo, categoryRepo, expenseId) as T
    }
}
