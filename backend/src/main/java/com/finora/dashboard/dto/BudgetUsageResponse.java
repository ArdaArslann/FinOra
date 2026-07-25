package com.finora.dashboard.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetUsageResponse(

        UUID categoryId,

        String categoryName,

        String categoryIcon,

        String categoryColor,

        BigDecimal budget,

        BigDecimal spent,

        BigDecimal remaining,

        int percentage

) {}
