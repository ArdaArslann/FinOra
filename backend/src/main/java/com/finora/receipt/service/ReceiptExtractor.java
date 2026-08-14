package com.finora.receipt.service;

public interface ReceiptExtractor {

    ReceiptExtractionResult extract(
            byte[] file,
            String contentType
    );
}