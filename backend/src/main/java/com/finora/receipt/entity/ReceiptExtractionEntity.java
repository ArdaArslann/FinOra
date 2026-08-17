package com.finora.receipt.entity;

import com.finora.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "receipt_extractions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReceiptExtractionEntity
        extends BaseEntity {

    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "receipt_id",
            nullable = false,
            unique = true
    )
    private ReceiptEntity receipt;

    @Column(length = 255)
    private String merchantName;

    @Column(
            precision = 19,
            scale = 2
    )
    private BigDecimal totalAmount;

    private LocalDate transactionDate;

    @Column(length = 10)
    private String currency;

    @Column(length = 100)
    private String suggestedCategory;

    private ReceiptExtractionEntity(
            ReceiptEntity receipt
    ) {
        this.receipt = receipt;
    }

    public static ReceiptExtractionEntity create(
            ReceiptEntity receipt
    ) {
        return new ReceiptExtractionEntity(
                receipt
        );
    }

    public void updateExtraction(
            String merchantName,
            BigDecimal totalAmount,
            LocalDate transactionDate,
            String currency,
            String suggestedCategory
    ) {

        this.merchantName =
                merchantName;

        this.totalAmount =
                totalAmount;

        this.transactionDate =
                transactionDate;

        this.currency =
                currency;

        this.suggestedCategory =
                suggestedCategory;
    }
}