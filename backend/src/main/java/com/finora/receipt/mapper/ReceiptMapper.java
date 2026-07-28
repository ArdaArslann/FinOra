package com.finora.receipt.mapper;

import com.finora.receipt.dto.ReceiptResponse;
import com.finora.receipt.entity.ReceiptEntity;
import org.springframework.stereotype.Component;

@Component
public class ReceiptMapper {

    public ReceiptResponse toResponse(ReceiptEntity receipt) {

        return new ReceiptResponse(
                receipt.getId(),
                receipt.getOriginalFileName(),
                receipt.getContentType(),
                receipt.getFileSize(),
                receipt.getStatus(),
                receipt.getUploadedAt()
        );
    }

}
