package com.finora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.finora.app.navigation.FinOraNavGraph
import com.finora.app.ui.theme.FinOraTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.finora.app.data.local.TokenManager
import kotlinx.coroutines.launch
import com.finora.app.navigation.Screen
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinOraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val scope = rememberCoroutineScope()
                    
                    val token by tokenManager.accessToken.collectAsState(initial = "loading")
                    val isOnboardingCompleted by tokenManager.isOnboardingCompleted.collectAsState(initial = null)
                    
                    if (isOnboardingCompleted == null || token == "loading") {
                        // wait for datastore to load
                        return@Surface
                    }
                    
                    LaunchedEffect(token) {
                        if (token == null) {
                            val currentRoute = navController.currentDestination?.route
                            if (currentRoute != null && 
                                currentRoute != Screen.Login.route && 
                                currentRoute != Screen.Onboarding.route &&
                                currentRoute != Screen.Register.route) {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    }

                    val startDest = if (token != null) {
                        Screen.Main.route
                    } else if (isOnboardingCompleted == true) {
                        Screen.Login.route
                    } else {
                        Screen.Onboarding.route
                    }

                    FinOraNavGraph(
                        navController = navController,
                        startDestination = startDest,
                        onOnboardingCompleted = {
                            scope.launch {
                                tokenManager.setOnboardingCompleted()
                            }
                        }
                    )
                }
            }
        }
    }
}
