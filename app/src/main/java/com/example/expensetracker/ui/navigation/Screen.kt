package com.example.expensetracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Dashboard : Screen("dashboard", "概览", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    data object ExpenseList : Screen("expenses", "明细", Icons.Filled.ListAlt, Icons.Outlined.ListAlt)
    data object Categories : Screen("categories", "分类", Icons.Filled.Category, Icons.Outlined.Category)
    data object AddExpense : Screen("add_expense", "记一笔", Icons.Filled.Add, Icons.Filled.Add)
    data object EditExpense : Screen("edit_expense/{expenseId}", "编辑", Icons.Filled.Add, Icons.Filled.Add) {
        fun createRoute(expenseId: Long) = "edit_expense/$expenseId"
    }

    companion object {
        val bottomTabs = listOf(Dashboard, ExpenseList, Categories)
    }
}
