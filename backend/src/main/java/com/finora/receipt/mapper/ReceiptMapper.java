package com.finora.receipt.mapper;

import com.finora.receipt.dto.ReceiptExtractionResponse;
import com.finora.receipt.dto.ReceiptResponse;
import com.finora.receipt.entity.ReceiptEntity;
import com.finora.receipt.entity.ReceiptExtractionEntity;
import org.springframework.stereotype.Component;

@Component
public class ReceiptMapper {

    public ReceiptResponse toResponse(
            ReceiptEntity receipt
    ) {

        ReceiptExtractionEntity extraction =
                receipt.getExtraction();

        ReceiptExtractionResponse extractionResponse =
                extraction != null
                        ? new ReceiptExtractionResponse(

                        extraction.getMerchantName(),

                        extraction.getTotalAmount(),

                        extraction.getTransactionDate(),

                        extraction.getCurrency(),

                        extraction.getSuggestedCategory()

                )
                        : null;

        return new ReceiptResponse(

                receipt.getId(),

                receipt.getOriginalFileName(),

                receipt.getContentType(),

                receipt.getFileSize(),

                receipt.getStatus(),

                receipt.getUploadedAt(),

                receipt.getTransaction() != null
                        ? receipt.getTransaction().getId()
                        : null,

                extractionResponse
        );
    }
}