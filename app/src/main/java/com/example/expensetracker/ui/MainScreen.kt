package com.example.expensetracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.ui.navigation.AppNavHost
import com.example.expensetracker.ui.navigation.Screen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showFab = currentRoute != null && currentRoute in listOf(Screen.Dashboard.route, Screen.ExpenseList.route)

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Screen.Dashboard.route,
                    onClick = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (currentRoute == Screen.Dashboard.route) Screen.Dashboard.selectedIcon else Screen.Dashboard.unselectedIcon,
                            contentDescription = Screen.Dashboard.title
                        )
                    },
                    label = { Text(Screen.Dashboard.title) }
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.ExpenseList.route,
                    onClick = {
                        navController.navigate(Screen.ExpenseList.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (currentRoute == Screen.ExpenseList.route) Screen.ExpenseList.selectedIcon else Screen.ExpenseList.unselectedIcon,
                            contentDescription = Screen.ExpenseList.title
                        )
                    },
                    label = { Text(Screen.ExpenseList.title) }
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.Categories.route,
                    onClick = {
                        navController.navigate(Screen.Categories.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (currentRoute == Screen.Categories.route) Screen.Categories.selectedIcon else Screen.Categories.unselectedIcon,
                            contentDescription = Screen.Categories.title
                        )
                    },
                    label = { Text(Screen.Categories.title) }
                )
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddExpense.route) }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "记一笔")
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
