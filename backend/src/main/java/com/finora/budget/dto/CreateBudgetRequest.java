package com.finora.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import com.finora.budget.enums.BudgetPeriod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateBudgetRequest(

        @NotNull
        @DecimalMin("0.01")
        BigDecimal amount,

        @NotNull
        BudgetPeriod period,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @NotNull
        UUID categoryId

) {
}