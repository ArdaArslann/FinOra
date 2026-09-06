package com.finora.app.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finora.app.data.network.CategoryStatisticDto
import com.finora.app.data.network.MonthlyStatisticDto
import com.finora.app.domain.model.Resource
import com.finora.app.ui.components.GlassCard
import com.finora.app.ui.theme.PrimaryNeon
import com.finora.app.ui.theme.SecondaryNeon
import com.finora.app.ui.theme.SpaceDark
import com.finora.app.ui.viewmodels.StatisticsViewModel

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val statsState by viewModel.statisticsState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statsState) {
        if (statsState is Resource.Error) {
            snackbarHostState.showSnackbar(
                message = statsState.message ?: "An error occurred"
            )
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
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Statistics",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (statsState is Resource.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryNeon)
                }
            } else if (statsState is Resource.Success) {
                val data = statsState.data!!

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item {
                        Text(
                            text = "Monthly Overview",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        MonthlyBarChart(monthlyStats = data.monthlyStats)
                    }

                    item {
                        Text(
                            text = "Expenses by Category",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (data.categoryStats.isNullOrEmpty()) {
                        item {
                            Text(
                                "No category data available.",
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        items(data.categoryStats) { catStat ->
                            CategoryStatItem(stat = catStat)
                        }
                    }
                }
            } else if (statsState is Resource.Error) {
                // Error is handled by snackbar
        }
    }
}
}

@Composable
fun MonthlyBarChart(monthlyStats: List<MonthlyStatisticDto>?) {
        if (monthlyStats.isNullOrEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No data to show", color = Color.White.copy(alpha = 0.5f))
                }
            }
            return
        }

        val maxAmount = monthlyStats.maxOfOrNull { maxOf(it.income, it.expense) }?.toFloat() ?: 1f

        GlassCard(modifier = Modifier.fillMaxWidth().height(250.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(10.dp).background(
                                PrimaryNeon,
                                androidx.compose.foundation.shape.CircleShape
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Income", color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(10.dp).background(
                                SecondaryNeon,
                                androidx.compose.foundation.shape.CircleShape
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Expense", color = Color.White, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barWidth = (canvasWidth / (monthlyStats.size * 3)).coerceAtMost(40f)
                    val spacing =
                        (canvasWidth - (monthlyStats.size * barWidth * 2)) / (monthlyStats.size + 1)

                    var currentX = spacing

                    monthlyStats.forEach { stat ->
                        val incomeHeight = (stat.income.toFloat() / maxAmount) * canvasHeight
                        val expenseHeight = (stat.expense.toFloat() / maxAmount) * canvasHeight

                        // Draw Income Bar
                        drawRoundRect(
                            color = PrimaryNeon,
                            topLeft = Offset(currentX, canvasHeight - incomeHeight),
                            size = Size(barWidth, incomeHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        // Draw Expense Bar
                        drawRoundRect(
                            color = SecondaryNeon,
                            topLeft = Offset(
                                currentX + barWidth + 4.dp.toPx(),
                                canvasHeight - expenseHeight
                            ),
                            size = Size(barWidth, expenseHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        currentX += (barWidth * 2) + spacing
                    }
                }
            }
        }
    }

@Composable
fun CategoryStatItem(stat: CategoryStatisticDto) {
        val percentageFloat = (stat.percentage / 100f).toFloat().coerceIn(0f, 1f)
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stat.categoryName,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        "₺${String.format("%.2f", stat.totalAmount)}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { percentageFloat },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = SecondaryNeon,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${String.format("%.1f", stat.percentage)}%",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        }
    }

