package com.finora.dashboard.dto;

import java.math.BigDecimal;

public record DashboardSummaryResponse(

        BigDecimal balance,

        BigDecimal totalIncome,

        BigDecimal totalExpense

) {}
