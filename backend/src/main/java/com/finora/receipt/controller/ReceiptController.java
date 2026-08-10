package com.finora.receipt.controller;

import com.finora.receipt.dto.ReceiptResponse;
import com.finora.receipt.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping(consumes = "multipart/form-data")
    public ReceiptResponse upload(
            @RequestParam("file") MultipartFile file
    ) {
        return receiptService.upload(file);
    }

    @GetMapping
    public List<ReceiptResponse> getAll() {
        return receiptService.getAll();
    }

    @GetMapping("/{id}")
    public ReceiptResponse getById(
            @PathVariable UUID id
    ) {
        return receiptService.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id
    ) {
        receiptService.delete(id);
    }
}