package com.finora.receipt.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ConfirmReceiptRequest(

        @NotNull
        @Positive
        BigDecimal amount,

        @Size(max = 255)
        String description,

        @NotNull
        LocalDate transactionDate,

        @NotNull
        UUID categoryId

) {
}