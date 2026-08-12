package com.finora.transaction.repository;

import com.finora.category.entity.CategoryEntity;
import com.finora.dashboard.projection.BudgetSpentProjection;
import com.finora.transaction.entity.TransactionEntity;
import com.finora.transaction.enums.TransactionType;
import com.finora.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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


    List<TransactionEntity> findTop5ByUserOrderByTransactionDateDesc(UserEntity user);

    @Query("""
    SELECT new com.finora.dashboard.projection.BudgetSpentProjection(
        b.id,
        COALESCE(SUM(t.amount), 0)
    )
    FROM BudgetEntity b
    LEFT JOIN TransactionEntity t
        ON t.user = b.user
        AND t.category = b.category
        AND t.type = :type
        AND t.transactionDate BETWEEN b.startDate AND b.endDate
    WHERE b.user = :user
    GROUP BY b.id
""")
    List<BudgetSpentProjection> findBudgetSpentByUser(
            @Param("user") UserEntity user,
            @Param("type") TransactionType type
    );
}