package com.finora.budget.mapper;

import com.finora.budget.dto.BudgetResponse;
import com.finora.budget.dto.CreateBudgetRequest;
import com.finora.budget.entity.BudgetEntity;
import com.finora.category.entity.CategoryEntity;
import com.finora.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class BudgetMapper {

    public BudgetResponse toResponse(BudgetEntity entity) {
        return new BudgetResponse(
                entity.getId(),
                entity.getAmount(),
                entity.getPeriod(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getCategory().getId(),
                entity.getCategory().getName(),
                entity.getCategory().getIcon(),
                entity.getCategory().getColor(),
                entity.getSpent() != null ? entity.getSpent() : java.math.BigDecimal.ZERO
        );
    }

    public BudgetEntity toEntity(
            CreateBudgetRequest request,
            CategoryEntity category,
            UserEntity user
    ) {
        return BudgetEntity.create(
                request.amount(),
                request.period(),
                request.startDate(),
                request.endDate(),
                category,
                user
        );
    }
}