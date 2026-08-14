package com.finora.receipt.service;

import com.finora.receipt.dto.ConfirmReceiptRequest;
import com.finora.receipt.dto.ReceiptResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import com.finora.transaction.dto.TransactionResponse;

public interface ReceiptService {

    ReceiptResponse upload(MultipartFile file);

    List<ReceiptResponse> getAll();

    ReceiptResponse getById(UUID id);

    void delete(UUID id);

    TransactionResponse confirmReceipt(
            UUID id,
            ConfirmReceiptRequest request
    );}