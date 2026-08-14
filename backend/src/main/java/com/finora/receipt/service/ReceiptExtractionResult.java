package com.finora.receipt.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceiptExtractionResult(

        String merchantName,

        BigDecimal totalAmount,

        LocalDate transactionDate,

        String currency,

        String suggestedCategory

) {
}