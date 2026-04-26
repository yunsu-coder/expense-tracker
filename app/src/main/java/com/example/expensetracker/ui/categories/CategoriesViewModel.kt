package com.example.expensetracker.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.db.entity.CategoryEntity
import com.example.expensetracker.data.repository.CategoryInUseException
import com.example.expensetracker.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val showAddDialog: Boolean = false,
    val editingCategory: CategoryEntity? = null,
    val deleteTarget: Pair<CategoryEntity, Int>? = null // category + expense count
)

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    val categories = categoryRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true) }
    fun hideAddDialog() = _uiState.update { it.copy(showAddDialog = false) }

    fun startEdit(category: CategoryEntity) =
        _uiState.update { it.copy(editingCategory = category) }

    fun cancelEdit() = _uiState.update { it.copy(editingCategory = null) }

    fun saveCategory(name: String, colorHex: Long) {
        viewModelScope.launch {
            val editing = _uiState.value.editingCategory
            if (editing != null) {
                categoryRepository.update(editing.copy(name = name, colorHex = colorHex))
                _uiState.update { it.copy(editingCategory = null) }
            } else {
                val maxOrder = categories.value.maxOfOrNull { it.sortOrder } ?: 0
                categoryRepository.save(
                    CategoryEntity(name = name, colorHex = colorHex, sortOrder = maxOrder + 1)
                )
                _uiState.update { it.copy(showAddDialog = false) }
            }
        }
    }

    fun requestDelete(category: CategoryEntity) {
        viewModelScope.launch {
            val count = categoryRepository.getExpenseCount(category.id)
            _uiState.update { it.copy(deleteTarget = Pair(category, count)) }
        }
    }

    fun confirmDelete() {
        val target = _uiState.value.deleteTarget ?: return
        viewModelScope.launch {
            try {
                categoryRepository.delete(target.first)
            } catch (_: CategoryInUseException) { }
            _uiState.update { it.copy(deleteTarget = null) }
        }
    }

    fun dismissDelete() = _uiState.update { it.copy(deleteTarget = null) }

    class Factory(private val repo: CategoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CategoriesViewModel(repo) as T
    }
}
