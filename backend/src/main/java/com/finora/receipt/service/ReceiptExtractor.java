package com.finora.receipt.service;

public interface ReceiptExtractor {

    ReceiptExtractionResult extract(
            byte[] imageData,
            String contentType
    );
}