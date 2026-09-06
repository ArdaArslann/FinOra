package com.finora.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finora.app.domain.model.Resource
import com.finora.app.ui.components.GlassCard
import com.finora.app.ui.theme.ErrorRed
import com.finora.app.ui.theme.PrimaryNeon
import com.finora.app.ui.theme.SpaceDark
import com.finora.app.ui.viewmodels.UserViewModel

@Composable
fun ProfileScreen(
    onNavigateToCategories: () -> Unit,
    onLogoutSuccess: () -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    val userState by viewModel.userState.collectAsState()
    val logoutState by viewModel.logoutState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userState, logoutState) {
        if (userState is Resource.Error) {
            snackbarHostState.showSnackbar(userState.message ?: "Failed to load profile")
        }
        if (logoutState is Resource.Error) {
            snackbarHostState.showSnackbar(logoutState.message ?: "Failed to logout")
        }
        
        if (logoutState is Resource.Success && logoutState.data != null) {
            onLogoutSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SpaceDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Profile",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (userState is Resource.Loading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryNeon)
            }
        } else if (userState is Resource.Success) {
            val user = userState.data!!
            val initials = "${user.firstName.firstOrNull() ?: ""}${user.lastName.firstOrNull() ?: ""}".uppercase()

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(PrimaryNeon.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials, color = PrimaryNeon, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "${user.firstName} ${user.lastName}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = user.email, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                }
            }
        } else if (userState is Resource.Error) {
            // Handled by Snackbar
        }

        Spacer(modifier = Modifier.height(32.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = onNavigateToCategories,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Categories", tint = PrimaryNeon)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Manage Categories", color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Log Out", tint = ErrorRed)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Log Out", color = ErrorRed, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    if (logoutState is Resource.Loading) {
                        CircularProgressIndicator(color = ErrorRed, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }
    }
}
}
