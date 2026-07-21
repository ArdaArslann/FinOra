package com.finora.transaction.dto;

import com.finora.transaction.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(

        UUID id,

        BigDecimal amount,

        TransactionType type,

        String description,

        LocalDate transactionDate,

        UUID categoryId,

        String categoryName,

        String categoryIcon,

        String categoryColor

) {
}