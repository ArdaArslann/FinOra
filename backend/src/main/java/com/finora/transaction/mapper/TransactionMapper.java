package com.finora.transaction.mapper;

import com.finora.category.entity.CategoryEntity;
import com.finora.transaction.dto.CreateTransactionRequest;
import com.finora.transaction.dto.TransactionResponse;
import com.finora.transaction.entity.TransactionEntity;
import com.finora.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(TransactionEntity entity) {
        return new TransactionResponse(
                entity.getId(),
                entity.getAmount(),
                entity.getType(),
                entity.getDescription(),
                entity.getTransactionDate(),
                entity.getCategory().getId(),
                entity.getCategory().getName(),
                entity.getCategory().getIcon(),
                entity.getCategory().getColor()
        );
    }

    public TransactionEntity toEntity(
            CreateTransactionRequest request,
            CategoryEntity category,
            UserEntity user
    ) {
        return TransactionEntity.create(
                request.amount(),
                request.type(),
                request.description(),
                request.transactionDate(),
                category,
                user
        );
    }
}
