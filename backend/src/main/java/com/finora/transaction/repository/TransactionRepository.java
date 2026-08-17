package com.finora.transaction.repository;

import com.finora.category.entity.CategoryEntity;
import com.finora.dashboard.projection.BudgetSpentProjection;
import com.finora.dashboard.projection.CategorySpentProjection;
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

import com.finora.statistics.projection.DailyStatistics;
import com.finora.statistics.projection.MonthlyStatistics;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    List<TransactionEntity> findAllByUserOrderByTransactionDateDesc(UserEntity user);

    boolean existsByCategory(CategoryEntity category);

    Optional<TransactionEntity> findByIdAndUser(UUID id, UserEntity user);

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

    @Query("""
    SELECT new com.finora.dashboard.projection.CategorySpentProjection(
        t.category.id,
        t.category.name,
        SUM(t.amount)
    )
    FROM TransactionEntity t
    WHERE t.user = :user
      AND t.type = :type
      AND t.transactionDate BETWEEN :startDate AND :endDate
    GROUP BY t.category.id, t.category.name
    ORDER BY SUM(t.amount) DESC
""")
    List<CategorySpentProjection> findCategorySpendingByUserAndDateBetween(
            @Param("user") UserEntity user,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM TransactionEntity t
    WHERE t.user = :user
      AND t.type = :type
      AND t.transactionDate BETWEEN :startDate AND :endDate
""")
    BigDecimal sumAmountByUserAndTypeAndTransactionDateBetween(
            @Param("user") UserEntity user,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

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

    long countByUserAndTransactionDateBetween(UserEntity user, LocalDate startDate, LocalDate endDate);

    @Query("""
    SELECT 
        t.transactionDate,
        COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END), 0)
    FROM TransactionEntity t
    WHERE t.user = :user
      AND t.transactionDate BETWEEN :startDate AND :endDate
    GROUP BY t.transactionDate
    ORDER BY t.transactionDate ASC
""")
    List<Object[]> getDailyTrendRaw(
            @Param("user") UserEntity user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    default List<DailyStatistics> getDailyTrend(UserEntity user, LocalDate startDate, LocalDate endDate) {
        return getDailyTrendRaw(user, startDate, endDate).stream()
                .map(row -> new DailyStatistics(
                        (LocalDate) row[0],
                        row[1] == null ? BigDecimal.ZERO : new BigDecimal(row[1].toString()),
                        row[2] == null ? BigDecimal.ZERO : new BigDecimal(row[2].toString()),
                        row[3] == null ? BigDecimal.ZERO : new BigDecimal(row[3].toString())
                ))
                .toList();
    }

    @Query("""
    SELECT 
        FUNCTION('TO_CHAR', t.transactionDate, 'YYYY-MM'),
        COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END), 0)
    FROM TransactionEntity t
    WHERE t.user = :user
      AND t.transactionDate BETWEEN :startDate AND :endDate
    GROUP BY FUNCTION('TO_CHAR', t.transactionDate, 'YYYY-MM')
    ORDER BY FUNCTION('TO_CHAR', t.transactionDate, 'YYYY-MM') ASC
""")
    List<Object[]> getMonthlyTrendRaw(
            @Param("user") UserEntity user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    default List<MonthlyStatistics> getMonthlyTrend(UserEntity user, LocalDate startDate, LocalDate endDate) {
        return getMonthlyTrendRaw(user, startDate, endDate).stream()
                .map(row -> new MonthlyStatistics(
                        (String) row[0],
                        row[1] == null ? BigDecimal.ZERO : new BigDecimal(row[1].toString()),
                        row[2] == null ? BigDecimal.ZERO : new BigDecimal(row[2].toString()),
                        row[3] == null ? BigDecimal.ZERO : new BigDecimal(row[3].toString())
                ))
                .toList();
    }
}