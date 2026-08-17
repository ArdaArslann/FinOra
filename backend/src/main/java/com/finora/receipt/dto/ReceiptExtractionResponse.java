package com.finora.receipt.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceiptExtractionResponse(

        String merchantName,

        BigDecimal totalAmount,

        LocalDate transactionDate,

        String currency,

        String suggestedCategory

) {
}