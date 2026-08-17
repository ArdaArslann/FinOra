package com.finora.dashboard.ai.response;

import java.math.BigDecimal;
import java.util.List;

public record FinancialInsightResponse(

        String summary,

        MonthlyStatus monthlyStatus,

        List<BudgetInsight> budgetInsights,

        List<String> recommendations

) {

    public record MonthlyStatus(

            BigDecimal income,

            BigDecimal expenses,

            BigDecimal balance

    ) {}

    public record BudgetInsight(

            String category,

            BigDecimal spent,

            BigDecimal budget,

            BigDecimal remaining,

            int usagePercentage

    ) {}
}