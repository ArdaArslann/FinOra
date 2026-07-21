package com.finora.transaction.entity;

import com.finora.category.entity.CategoryEntity;
import com.finora.common.entity.BaseEntity;
import com.finora.transaction.enums.TransactionType;
import com.finora.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionEntity extends BaseEntity {

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private TransactionEntity(
            BigDecimal amount,
            TransactionType type,
            String description,
            LocalDate transactionDate,
            CategoryEntity category,
            UserEntity user
    ) {
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.transactionDate = transactionDate;
        this.category = category;
        this.user = user;
    }

    public static TransactionEntity create(
            BigDecimal amount,
            TransactionType type,
            String description,
            LocalDate transactionDate,
            CategoryEntity category,
            UserEntity user
    ) {
        return new TransactionEntity(
                amount,
                type,
                description,
                transactionDate,
                category,
                user
        );
    }

    public void update(
            BigDecimal amount,
            TransactionType type,
            String description,
            LocalDate transactionDate,
            CategoryEntity category
    ) {
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.transactionDate = transactionDate;
        this.category = category;
    }
}