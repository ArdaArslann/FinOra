package com.finora.budget.dto;

import com.finora.budget.enums.BudgetPeriod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BudgetResponse(

        UUID id,

        BigDecimal amount,

        BudgetPeriod period,

        LocalDate startDate,

        LocalDate endDate,

        UUID categoryId,

        String categoryName,

        String categoryIcon,

        String categoryColor

) {
}