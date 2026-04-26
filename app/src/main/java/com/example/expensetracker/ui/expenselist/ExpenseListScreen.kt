package com.example.expensetracker.ui.expenselist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.ExpenseTrackerApp
import com.example.expensetracker.ui.components.CategorySelector
import com.example.expensetracker.ui.components.ConfirmDeleteDialog
import com.example.expensetracker.ui.components.EmptySearchIcon
import com.example.expensetracker.ui.components.EmptyState
import com.example.expensetracker.ui.components.ExpenseItem
import com.example.expensetracker.ui.components.ListSkeleton
import com.example.expensetracker.ui.components.NoExpensesIcon
import com.example.expensetracker.util.toLocalDateCompat
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onNavigateToEdit: (Long) -> Unit = {},
    onNavigateToAddExpense: () -> Unit = {}
) {
    val app = LocalContext.current.applicationContext as ExpenseTrackerApp
    val viewModel: ExpenseListViewModel = viewModel(
        factory = ExpenseListViewModel.Factory(
            app.container.expenseRepository,
            app.container.categoryRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val listState = rememberLazyListState()

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("支出明细") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Category filter
            CategorySelector(
                categories = categories,
                selectedId = uiState.selectedCategoryId,
                onSelect = { viewModel.selectCategory(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Date range
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = { showStartDatePicker = true }) {
                    Text("开始: ${uiState.startDate}")
                }
                Text("~")
                TextButton(onClick = { showEndDatePicker = true }) {
                    Text("结束: ${uiState.endDate}")
                }
                Text(
                    "${uiState.totalCount} 条",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            // Expense list
            if (uiState.isLoading && uiState.expenses.isEmpty()) {
                ListSkeleton(Modifier.fillMaxSize())
            } else if (uiState.expenses.isEmpty()) {
                val hasFilters = uiState.selectedCategoryId != null
                EmptyState(
                    icon = if (hasFilters) EmptySearchIcon else NoExpensesIcon,
                    title = if (hasFilters) "没有符合条件的支出" else "还没有支出记录",
                    subtitle = if (hasFilters) "试试调整筛选条件" else "点击右下角 + 记一笔",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.expenses) { expense ->
                        ExpenseItem(
                            categoryName = expense.categoryName,
                            categoryColor = expense.categoryColor,
                            amount = expense.amount,
                            note = expense.note,
                            date = expense.date.toLocalDateCompat().toString(),
                            onClick = { onNavigateToEdit(expense.id) }
                        )
                    }

                    if (uiState.hasMore) {
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator()
                                } else {
                                    Button(onClick = { viewModel.loadNextPage() }) {
                                        Text("加载更多")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation
    uiState.deleteTarget?.let { expense ->
        ConfirmDeleteDialog(
            title = "删除支出",
            message = "确定删除「${expense.categoryName}」的 ¥${"%.2f".format(expense.amount)} 支出吗？",
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.dismissDelete() }
        )
    }

    // Start date picker
    if (showStartDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.startDate.atStartOfDay(ZoneId.of("Asia/Shanghai"))
                .toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("Asia/Shanghai")).toLocalDate()
                        viewModel.setDateRange(date, uiState.endDate)
                    }
                    showStartDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("取消") } }
        ) { DatePicker(state = pickerState) }
    }

    // End date picker
    if (showEndDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.endDate.atStartOfDay(ZoneId.of("Asia/Shanghai"))
                .toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("Asia/Shanghai")).toLocalDate()
                        viewModel.setDateRange(uiState.startDate, date)
                    }
                    showEndDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("取消") } }
        ) { DatePicker(state = pickerState) }
    }
}
