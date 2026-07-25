package com.finora.dashboard.dto;

import java.util.List;

public record DashboardResponse(

        DashboardSummaryResponse summary,

        List<BudgetUsageResponse> budgets,

        List<RecentTransactionResponse> recentTransactions,

        String aiInsight

) {}
