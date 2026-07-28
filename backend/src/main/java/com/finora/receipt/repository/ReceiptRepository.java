package com.finora.receipt.repository;

import com.finora.receipt.entity.ReceiptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReceiptRepository extends JpaRepository<ReceiptEntity, UUID> {
}
