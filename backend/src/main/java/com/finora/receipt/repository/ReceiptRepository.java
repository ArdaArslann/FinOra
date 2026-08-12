package com.finora.receipt.repository;

import com.finora.receipt.entity.ReceiptEntity;
import com.finora.transaction.entity.TransactionEntity;
import com.finora.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceiptRepository
        extends JpaRepository<ReceiptEntity, UUID> {

    List<ReceiptEntity> findAllByUserOrderByUploadedAtDesc(
            UserEntity user
    );

    Optional<ReceiptEntity> findByIdAndUser(
            UUID id,
            UserEntity user
    );

    List<ReceiptEntity> findAllByTransaction(
            TransactionEntity transaction
    );
}