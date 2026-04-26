package com.example.expensetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.expensetracker.ui.addexpense.AddExpenseScreen
import com.example.expensetracker.ui.categories.CategoriesScreen
import com.example.expensetracker.ui.dashboard.DashboardScreen
import com.example.expensetracker.ui.expenselist.ExpenseListScreen

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.route) }
            )
        }
        composable(Screen.ExpenseList.route) {
            ExpenseListScreen(
                onNavigateToEdit = { expenseId ->
                    navController.navigate(Screen.EditExpense.createRoute(expenseId))
                },
                onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.route) }
            )
        }
        composable(Screen.Categories.route) {
            CategoriesScreen()
        }
        composable(Screen.AddExpense.route) {
            AddExpenseScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.EditExpense.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getLong("expenseId")
            AddExpenseScreen(
                expenseId = expenseId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
