package com.finora.dashboard.projection;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetSpentProjection(
        UUID budgetId,
        BigDecimal spent
) {}
