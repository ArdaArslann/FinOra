package com.finora.dashboard.ai.context;

import java.math.BigDecimal;

public record CategorySpendingContext(

        String categoryName,

        BigDecimal amount

) {
}