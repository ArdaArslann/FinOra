package com.finora.transaction.service;

import com.finora.transaction.dto.CreateTransactionRequest;
import com.finora.transaction.dto.TransactionResponse;
import com.finora.transaction.dto.UpdateTransactionRequest;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    TransactionResponse createTransaction(CreateTransactionRequest request);

    List<TransactionResponse> getTransactions();

    TransactionResponse getTransactionById(UUID id);

    TransactionResponse updateTransaction(
            UUID id,
            UpdateTransactionRequest request
    );

    void deleteTransaction(UUID id);

}