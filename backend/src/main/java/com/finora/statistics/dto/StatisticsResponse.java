package com.finora.statistics.dto;

import com.finora.statistics.projection.CategoryStatistics;
import com.finora.statistics.projection.DailyStatistics;
import com.finora.statistics.projection.MonthlyStatistics;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StatisticsResponse(
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        long transactionCount,
        List<CategoryStatistics> categoryBreakdown,
        List<DailyStatistics> dailyTrend,
        List<MonthlyStatistics> monthlyTrend,
        List<BudgetPerformance> budgetPerformance
) {
}
