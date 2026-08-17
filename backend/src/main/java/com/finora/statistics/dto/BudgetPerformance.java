package com.finora.statistics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetPerformance(
        UUID categoryId,
        String categoryName,
        BigDecimal budgetAmount,
        BigDecimal amountSpent,
        BigDecimal remainingAmount,
        BigDecimal usagePercentage,
        String status
) {
}
