package com.finora.receipt.service;

import com.finora.receipt.dto.ReceiptResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ReceiptService {

    ReceiptResponse upload(MultipartFile file);

    List<ReceiptResponse> getAll();

    ReceiptResponse getById(UUID id);

    void delete(UUID id);
}