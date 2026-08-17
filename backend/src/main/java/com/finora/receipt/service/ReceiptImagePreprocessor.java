package com.finora.receipt.service;

public interface ReceiptImagePreprocessor {

    byte[] preprocess(
            byte[] file,
            String contentType
    );
}