package com.finora.dashboard.dto;

import com.finora.transaction.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecentTransactionResponse(

        UUID id,

        String description,

        BigDecimal amount,

        TransactionType type,

        LocalDate transactionDate,

        String categoryName,

        String categoryIcon

) {}
