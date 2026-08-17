package com.finora.receipt.service;

public interface ReceiptOcrService {

    String extractText(
            byte[] file,
            String contentType
    );
}