package com.finora.dashboard.ai.context;

import java.math.BigDecimal;

public record BudgetContext(

        String categoryName,

        BigDecimal budget,

        BigDecimal spent,

        BigDecimal remaining,

        int percentage

) {
}