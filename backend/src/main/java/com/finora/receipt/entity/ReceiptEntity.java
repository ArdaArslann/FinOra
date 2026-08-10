package com.finora.receipt.entity;

import com.finora.common.entity.BaseEntity;
import com.finora.common.exception.BusinessException;
import com.finora.receipt.enums.ReceiptStatus;
import com.finora.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.finora.transaction.entity.TransactionEntity;

import java.time.LocalDateTime;
@Entity
@Table(name = "receipts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReceiptEntity extends BaseEntity {

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false, unique = true)
    private String storageKey;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReceiptStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private TransactionEntity transaction;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    private ReceiptEntity(
            String originalFileName,
            String storageKey,
            String contentType,
            Long fileSize,
            UserEntity user
    ) {
        this.originalFileName = originalFileName;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.user = user;
        this.status = ReceiptStatus.UPLOADED;
        this.uploadedAt = LocalDateTime.now();
    }

    public static ReceiptEntity create(
            String originalFileName,
            String storageKey,
            String contentType,
            Long fileSize,
            UserEntity user
    ) {
        return new ReceiptEntity(
                originalFileName,
                storageKey,
                contentType,
                fileSize,
                user
        );
    }
    public void updateStatus(ReceiptStatus status) {
        this.status = status;
    }
    public void assignTransaction(TransactionEntity transaction) {

        if (this.transaction != null) {
            throw new BusinessException(
                    "RECEIPT_ALREADY_ASSIGNED",
                    "Receipt is already assigned to a transaction."
            );
        }

        this.transaction = transaction;
    }

}