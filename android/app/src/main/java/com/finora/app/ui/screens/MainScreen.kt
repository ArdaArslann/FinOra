package com.finora.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.finora.app.navigation.Screen
import com.finora.app.ui.screens.budget.BudgetScreen
import com.finora.app.ui.screens.dashboard.DashboardScreen
import com.finora.app.ui.screens.receipts.ReceiptScreen
import com.finora.app.ui.screens.statistics.StatisticsScreen
import com.finora.app.ui.screens.transactions.TransactionScreen
import com.finora.app.ui.theme.PrimaryNeon
import com.finora.app.ui.theme.SurfaceDark

@Composable
fun MainScreen(
    onNavigateToLogin: () -> Unit = {}
) {
    val navController = rememberNavController()
    
    val items = listOf(
        Screen.Dashboard,
        Screen.Transactions,
        Screen.Budgets,
        Screen.Statistics,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    val label = when(screen) {
                        Screen.Dashboard -> "Home"
                        Screen.Transactions -> "List"
                        Screen.Budgets -> "Budgets"
                        Screen.Statistics -> "Stats"
                        Screen.Profile -> "Profile"
                        else -> ""
                    }
                    val icon = when(screen) {
                        Screen.Dashboard -> Icons.Filled.Home
                        Screen.Transactions -> Icons.Filled.List
                        Screen.Budgets -> Icons.Filled.Settings
                        Screen.Statistics -> Icons.Filled.Star
                        Screen.Profile -> Icons.Filled.Person
                        else -> Icons.Filled.Home
                    }
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryNeon,
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = PrimaryNeon,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = SurfaceDark
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.Receipts.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                containerColor = PrimaryNeon
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Receipt", tint = Color.Black)
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Transactions.route) { TransactionScreen() }
            composable(Screen.Budgets.route) { BudgetScreen() }
            composable(Screen.Statistics.route) { StatisticsScreen() }
            composable(Screen.Receipts.route) { 
                ReceiptScreen(
                    onNavigateHome = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(0)
                        }
                    }
                ) 
            }
            composable(Screen.Profile.route) { 
                com.finora.app.ui.screens.profile.ProfileScreen(
                    onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                    onLogoutSuccess = onNavigateToLogin
                )
            }
            composable(Screen.Categories.route) {
                com.finora.app.ui.screens.category.CategoryScreen(
                    onNavigateBack = { navController.navigateUp() }
                )
            }
        }
    }
}
