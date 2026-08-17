package com.finora.statistics.projection;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryStatistics(
        UUID categoryId,
        String categoryName,
        BigDecimal amount,
        BigDecimal percentage
) {
    public CategoryStatistics withPercentage(BigDecimal percentage) {
        return new CategoryStatistics(this.categoryId, this.categoryName, this.amount, percentage);
    }
}
