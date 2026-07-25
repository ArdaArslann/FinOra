package com.finora.dashboard.mapper;

import com.finora.dashboard.dto.RecentTransactionResponse;
import com.finora.transaction.entity.TransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class DashboardMapper {

    public RecentTransactionResponse toRecentTransactionResponse(
            TransactionEntity transaction
    ) {
        return new RecentTransactionResponse(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getTransactionDate(),
                transaction.getCategory().getName(),
                transaction.getCategory().getIcon()
        );
    }

}
