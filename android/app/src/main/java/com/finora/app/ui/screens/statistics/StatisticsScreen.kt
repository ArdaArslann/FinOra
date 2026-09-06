package com.finora.app.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.finora.app.ui.theme.ErrorRed
import com.finora.app.ui.theme.SpaceDark
import com.finora.app.ui.viewmodels.StatisticsViewModel
import com.finora.app.ui.viewmodels.StatsPeriod

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val statsState by viewModel.statisticsState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.fetchStatistics()
    }

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

            Spacer(modifier = Modifier.height(20.dp))

            // Period Selector
            PeriodSelector(
                selected = selectedPeriod,
                onSelect = { viewModel.selectPeriod(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            val isLoading = statsState is Resource.Loading
            val data = statsState.data

            if (isLoading && data == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryNeon)
                }
            } else {
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = PrimaryNeon,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (data != null) {
                    val chartLabel = when (selectedPeriod) {
                        StatsPeriod.WEEKLY -> "Daily Overview (This Week)"
                        StatsPeriod.MONTHLY -> "Weekly Overview (This Month)"
                        StatsPeriod.YEARLY -> "Monthly Overview (This Year)"
                        StatsPeriod.ALL_TIME -> "Yearly Overview (All Time)"
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            Text(
                                text = chartLabel,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            MonthlyBarChart(monthlyStats = data.monthlyStats)
                        }

                        item {
                            Text(
                                text = "Expenses by Category",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (data.categoryStats.isNullOrEmpty()) {
                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "No expense transactions found for this period.",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            items(data.categoryStats) { catStat ->
                                CategoryStatItem(stat = catStat)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodSelector(
    selected: StatsPeriod,
    onSelect: (StatsPeriod) -> Unit
) {
    val periods = listOf(
        StatsPeriod.WEEKLY to "Weekly",
        StatsPeriod.MONTHLY to "Monthly",
        StatsPeriod.YEARLY to "Yearly",
        StatsPeriod.ALL_TIME to "All Time"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.07f)),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        periods.forEach { (period, label) ->
            val isSelected = selected == period
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) PrimaryNeon else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { onSelect(period) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
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
    var selectedBar by remember { mutableStateOf<MonthlyStatisticDto?>(null) }

    GlassCard(modifier = Modifier.fillMaxWidth().height(250.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                                ErrorRed,
                                androidx.compose.foundation.shape.CircleShape
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Expense", color = Color.White, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
    
                Canvas(
                    modifier = Modifier.fillMaxSize().pointerInput(monthlyStats) {
                        detectTapGestures { tapOffset ->
                            val canvasWidth = size.width.toFloat()
                            val barWidth = (canvasWidth / (monthlyStats.size * 3)).coerceAtMost(40f)
                            val spacing = (canvasWidth - (monthlyStats.size * barWidth * 2f)) / (monthlyStats.size + 1)
                            val blockWidth = (barWidth * 2f) + spacing
                            
                            val index = ((tapOffset.x - (spacing / 2f)) / blockWidth).toInt()
                            if (index in monthlyStats.indices) {
                                selectedBar = if (selectedBar == monthlyStats[index]) null else monthlyStats[index]
                            } else {
                                selectedBar = null
                            }
                        }
                    }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barWidth = (canvasWidth / (monthlyStats.size * 3)).coerceAtMost(40f)
                    val spacing = (canvasWidth - (monthlyStats.size * barWidth * 2)) / (monthlyStats.size + 1)
    
                    var currentX = spacing
    
                    monthlyStats.forEach { stat ->
                        val incomeHeight = (stat.income.toFloat() / maxAmount) * canvasHeight
                        val expenseHeight = (stat.expense.toFloat() / maxAmount) * canvasHeight
                        
                        val isSelected = selectedBar == stat
                        val alpha = if (selectedBar == null || isSelected) 1f else 0.3f
    
                        drawRoundRect(
                            color = PrimaryNeon.copy(alpha = alpha),
                            topLeft = Offset(currentX, canvasHeight - incomeHeight),
                            size = Size(barWidth, incomeHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
    
                        drawRoundRect(
                            color = ErrorRed.copy(alpha = alpha),
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
            
            // Tooltip Overlay
            androidx.compose.animation.AnimatedVisibility(
                visible = selectedBar != null,
                modifier = Modifier.align(Alignment.Center)
            ) {
                selectedBar?.let { stat ->
                    Box(
                        modifier = Modifier
                            .background(SpaceDark.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stat.month, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(PrimaryNeon, androidx.compose.foundation.shape.CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("₺${String.format("%.2f", stat.income)}", color = Color.White, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(ErrorRed, androidx.compose.foundation.shape.CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("₺${String.format("%.2f", stat.expense)}", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
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
                color = ErrorRed,
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

