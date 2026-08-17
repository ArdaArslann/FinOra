package com.finora.receipt.service;

import com.finora.receipt.domain.ParsedReceipt;

public interface ReceiptParser {

    ParsedReceipt parse(String ocrText);
}