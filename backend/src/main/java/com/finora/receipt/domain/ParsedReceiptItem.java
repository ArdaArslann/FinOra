package com.finora.receipt.domain;

import java.math.BigDecimal;

public record ParsedReceiptItem(
        String name,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal total
) {
}