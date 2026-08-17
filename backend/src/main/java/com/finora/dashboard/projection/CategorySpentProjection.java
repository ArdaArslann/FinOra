package com.finora.dashboard.projection;

import java.math.BigDecimal;
import java.util.UUID;

public record CategorySpentProjection(

        UUID categoryId,

        String categoryName,

        BigDecimal spent

) {
}