package com.example.expensetracker.ui.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.ExpenseTrackerApp
import com.example.expensetracker.data.db.entity.CategoryEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen() {
    val app = LocalContext.current.applicationContext as ExpenseTrackerApp
    val viewModel: CategoriesViewModel = viewModel(
        factory = CategoriesViewModel.Factory(app.container.categoryRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("分类管理") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, "添加分类")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (categories.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("还没有分类，点击右下角添加", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(categories) { category ->
                        CategoryRow(
                            category = category,
                            onEdit = { viewModel.startEdit(category) },
                            onDelete = { viewModel.requestDelete(category) }
                        )
                    }
                }
            }
        }
    }

    // Add dialog
    if (uiState.showAddDialog) {
        CategoryEditDialog(
            initialName = "",
            initialColor = 0xFF4CAF50,
            onSave = { name, color -> viewModel.saveCategory(name, color) },
            onDismiss = { viewModel.hideAddDialog() }
        )
    }

    // Edit dialog
    uiState.editingCategory?.let { category ->
        CategoryEditDialog(
            initialName = category.name,
            initialColor = category.colorHex,
            onSave = { name, color -> viewModel.saveCategory(name, color) },
            onDismiss = { viewModel.cancelEdit() }
        )
    }

    // Delete confirmation
    uiState.deleteTarget?.let { (category, count) ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDelete() },
            title = { Text("删除「${category.name}」") },
            text = {
                Text(
                    if (count > 0) "该分类下有 $count 笔支出，删除后这些支出将无法正常显示。确定删除吗？"
                    else "确定删除该分类吗？"
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDelete() }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun CategoryRow(
    category: CategoryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val colorHex = category.colorHex
                androidx.compose.foundation.Canvas(modifier = Modifier.size(24.dp)) {
                    drawCircle(color = Color(colorHex))
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                category.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onEdit) { Text("编辑") }
            TextButton(onClick = onDelete) {
                Text("删除", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun CategoryEditDialog(
    initialName: String,
    initialColor: Long,
    onSave: (String, Long) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedColor by remember { mutableStateOf(initialColor) }
    val presetColors = listOf(
        0xFF4CAF50L, 0xFF2196F3L, 0xFFFF9800L, 0xFF9C27B0L,
        0xFFF44336L, 0xFF00BCD4L, 0xFF795548L, 0xFF607D8B
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialName.isEmpty()) "添加分类" else "编辑分类") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("分类名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Text("选择颜色", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presetColors.forEach { color ->
                        val isSelected = color == selectedColor
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.size(if (isSelected) 32.dp else 28.dp)) {
                                drawCircle(color = Color(color))
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onSave(name.trim(), selectedColor) },
                enabled = name.isNotBlank()
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
