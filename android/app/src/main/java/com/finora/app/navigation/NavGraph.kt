package com.finora.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.finora.app.ui.screens.OnboardingScreen
import com.finora.app.ui.screens.auth.LoginScreen
import com.finora.app.ui.screens.dashboard.DashboardScreen
import com.finora.app.ui.screens.transactions.TransactionScreen

import com.finora.app.ui.screens.receipts.ReceiptScreen
import com.finora.app.ui.screens.auth.RegisterScreen
import com.finora.app.ui.screens.budget.BudgetScreen
import com.finora.app.ui.screens.statistics.StatisticsScreen

@Composable
fun FinOraNavGraph(
    navController: NavHostController, 
    startDestination: String = Screen.Onboarding.route,
    onOnboardingCompleted: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStartedClick = {
                    onOnboardingCompleted()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Main.route) {
            com.finora.app.ui.screens.MainScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
