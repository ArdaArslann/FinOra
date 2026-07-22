package com.finora.budget.repository;

import com.finora.budget.entity.BudgetEntity;
import com.finora.category.entity.CategoryEntity;
import com.finora.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<BudgetEntity, UUID> {

    List<BudgetEntity> findAllByUserOrderByStartDateDesc(UserEntity user);

    Optional<BudgetEntity> findByIdAndUser(UUID id, UserEntity user);

    boolean existsByCategory(CategoryEntity category);

    boolean existsByCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            CategoryEntity category,
            LocalDate endDate,
            LocalDate startDate
    );
}