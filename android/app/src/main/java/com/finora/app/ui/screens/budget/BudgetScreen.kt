package com.finora.app.ui.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finora.app.data.network.BudgetDto
import com.finora.app.domain.model.Resource
import com.finora.app.ui.components.GlassCard
import com.finora.app.ui.theme.ErrorRed
import com.finora.app.ui.theme.PrimaryNeon
import com.finora.app.ui.theme.SpaceDark
import com.finora.app.ui.theme.SuccessGreen
import com.finora.app.ui.viewmodels.BudgetViewModel

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val budgetState by viewModel.budgets.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceDark)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Budgets",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        if (budgetState is Resource.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryNeon)
            }
        } else if (budgetState is Resource.Success) {
            val budgets = budgetState.data ?: emptyList()
            if (budgets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No budgets found.", color = Color.White.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(budgets) { budget ->
                        BudgetItem(budget)
                    }
                }
            }
        } else if (budgetState is Resource.Error) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(budgetState.message ?: "An error occurred", color = ErrorRed, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun BudgetItem(budget: BudgetDto) {
    // We mock the spent amount for UI demonstration until we join it with transactions properly
    val spent = budget.amount * 0.75 
    val percentage = (spent / budget.amount).toFloat()
    
    val progressColor = when {
        percentage > 0.9f -> ErrorRed
        percentage > 0.7f -> Color(0xFFF59E0B) // Amber
        else -> SuccessGreen
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(budget.categoryId, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text("$${String.format("%.2f", spent)} / $${String.format("%.2f", budget.amount)}", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { percentage },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = progressColor,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("${(percentage * 100).toInt()}% used", color = progressColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
