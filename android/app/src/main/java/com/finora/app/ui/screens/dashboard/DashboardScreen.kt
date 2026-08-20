package com.finora.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import com.finora.app.domain.model.Resource
import com.finora.app.ui.components.GlassCard
import com.finora.app.ui.theme.PrimaryNeon
import com.finora.app.ui.theme.SecondaryNeon
import com.finora.app.ui.theme.SpaceDark
import com.finora.app.ui.theme.SuccessGreen
import com.finora.app.ui.viewmodels.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val summaryState by viewModel.summaryState.collectAsState()
    val insightState by viewModel.insightState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Dashboard",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        if (summaryState is Resource.Loading) {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryNeon)
            }
        } else if (summaryState is Resource.Success) {
            val data = summaryState.data!!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GlassCard(
                    modifier = Modifier.weight(1f).height(180.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Total Balance", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$${data.currentBalance}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("${if (data.incomePercentageChange > 0) "+" else ""}${data.incomePercentageChange}% this month", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GlassCard(
                    modifier = Modifier.weight(1f).height(140.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Income", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$${data.totalIncome}", color = PrimaryNeon, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                GlassCard(
                    modifier = Modifier.weight(1f).height(140.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Expenses", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$${data.totalExpense}", color = SecondaryNeon, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "AI Insights",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        if (insightState is Resource.Loading) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SecondaryNeon)
            }
        } else if (insightState is Resource.Success) {
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = insightState.data?.summary ?: "No insights yet.",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
