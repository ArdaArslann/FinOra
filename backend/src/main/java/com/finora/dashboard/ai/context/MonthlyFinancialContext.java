package com.finora.dashboard.ai.context;

import java.math.BigDecimal;

public record MonthlyFinancialContext(

        BigDecimal income,

        BigDecimal expense,

        BigDecimal balance

) {
}