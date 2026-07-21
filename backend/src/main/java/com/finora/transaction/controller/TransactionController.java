package com.finora.transaction.controller;

import com.finora.common.dto.ApiResponse;
import com.finora.transaction.dto.CreateTransactionRequest;
import com.finora.transaction.dto.TransactionResponse;
import com.finora.transaction.dto.UpdateTransactionRequest;
import com.finora.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request
    ) {

        TransactionResponse response =
                transactionService.createTransaction(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions() {

        List<TransactionResponse> response =
                transactionService.getTransactions();

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @PathVariable UUID id
    ) {

        TransactionResponse response =
                transactionService.getTransactionById(id);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequest request
    ) {

        TransactionResponse response =
                transactionService.updateTransaction(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable UUID id
    ) {

        transactionService.deleteTransaction(id);

        return ResponseEntity.noContent().build();
    }

}