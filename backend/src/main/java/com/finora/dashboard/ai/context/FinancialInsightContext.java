package com.finora.dashboard.ai.context;

import java.math.BigDecimal;
import java.util.List;

public record FinancialInsightContext(

        BigDecimal totalIncome,

        BigDecimal totalExpense,

        BigDecimal balance,

        MonthlyFinancialContext monthly,

        List<CategorySpendingContext> categorySpendings,

        List<BudgetContext> budgets

){}