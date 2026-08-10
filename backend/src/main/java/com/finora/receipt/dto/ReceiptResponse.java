package com.finora.receipt.dto;

import com.finora.receipt.enums.ReceiptStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReceiptResponse(

        UUID id,

        String originalFileName,

        String contentType,

        Long fileSize,

        ReceiptStatus status,

        LocalDateTime uploadedAt,

        UUID transactionId

){}