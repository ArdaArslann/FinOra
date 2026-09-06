package com.finora.app.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Dashboard : Screen("dashboard")
    object Transactions : Screen("transactions")
    object Receipts : Screen("receipts")
    object Profile : Screen("profile")
    object Categories : Screen("categories")
    object Budgets : Screen("budgets")
    object Statistics : Screen("statistics")
}
