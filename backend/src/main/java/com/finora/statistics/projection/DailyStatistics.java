package com.finora.statistics.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyStatistics(
        LocalDate date,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance
) {
    public DailyStatistics(LocalDate date, Number income, Number expense, Number balance) {
        this(
                date,
                income == null ? BigDecimal.ZERO : new BigDecimal(income.toString()),
                expense == null ? BigDecimal.ZERO : new BigDecimal(expense.toString()),
                balance == null ? BigDecimal.ZERO : new BigDecimal(balance.toString())
        );
    }
}
