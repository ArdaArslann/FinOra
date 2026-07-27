package com.finora.transaction.repository;

import com.finora.category.entity.CategoryEntity;
import com.finora.transaction.entity.TransactionEntity;
import com.finora.transaction.enums.TransactionType;
import com.finora.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    List<TransactionEntity> findAllByUserOrderByTransactionDateDesc(UserEntity user);

    boolean existsByCategory(CategoryEntity category);

    Optional<TransactionEntity> findByIdAndUser(UUID id, UserEntity user);

    @Query("""

            SELECT COALESCE(SUM(t.amount), 0)

            FROM TransactionEntity t

            WHERE t.user = :user

            AND t.type = :type

            """)
    BigDecimal sumAmountByUserAndType(

            @Param("user") UserEntity user,

            @Param("type") TransactionType type

    );

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM TransactionEntity t
    WHERE t.user = :user
      AND t.category = :category
      AND t.type = :type
      AND t.transactionDate BETWEEN :startDate AND :endDate
""")
    BigDecimal sumAmountByUserAndCategoryAndTypeAndTransactionDateBetween(
            @Param("user") UserEntity user,
            @Param("category") CategoryEntity category,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    List<TransactionEntity> findTop5ByUserOrderByTransactionDateDesc(UserEntity user);

}