package com.finora.receipt.repository;

import com.finora.receipt.entity.ReceiptEntity;
import com.finora.receipt.entity.ReceiptExtractionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReceiptExtractionRepository
        extends JpaRepository<ReceiptExtractionEntity, UUID> {

    Optional<ReceiptExtractionEntity> findByReceipt(
            ReceiptEntity receipt
    );
}
