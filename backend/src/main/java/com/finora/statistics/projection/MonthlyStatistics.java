package com.finora.statistics.projection;

import java.math.BigDecimal;

public record MonthlyStatistics(
        String yearMonth,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance
) {
    public MonthlyStatistics(String yearMonth, Number income, Number expense, Number balance) {
        this(
                yearMonth,
                income == null ? BigDecimal.ZERO : new BigDecimal(income.toString()),
                expense == null ? BigDecimal.ZERO : new BigDecimal(expense.toString()),
                balance == null ? BigDecimal.ZERO : new BigDecimal(balance.toString())
        );
    }
}
