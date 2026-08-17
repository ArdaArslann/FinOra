package com.finora.dashboard.ai.context;

import com.finora.budget.entity.BudgetEntity;
import com.finora.budget.repository.BudgetRepository;
import com.finora.transaction.enums.TransactionType;
import com.finora.transaction.repository.TransactionRepository;
import com.finora.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinancialInsightContextBuilder {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    public FinancialInsightContext build(UserEntity user) {

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.withDayOfMonth(1);

        BigDecimal totalIncome =
                transactionRepository.sumAmountByUserAndType(
                        user,
                        TransactionType.INCOME
                );

        BigDecimal totalExpense =
                transactionRepository.sumAmountByUserAndType(
                        user,
                        TransactionType.EXPENSE
                );

        BigDecimal monthlyIncome =
                transactionRepository
                        .sumAmountByUserAndTypeAndTransactionDateBetween(
                                user,
                                TransactionType.INCOME,
                                startDate,
                                endDate
                        );

        BigDecimal monthlyExpense =
                transactionRepository
                        .sumAmountByUserAndTypeAndTransactionDateBetween(
                                user,
                                TransactionType.EXPENSE,
                                startDate,
                                endDate
                        );

        BigDecimal monthlyBalance =
                monthlyIncome.subtract(monthlyExpense);

        MonthlyFinancialContext monthly =
                new MonthlyFinancialContext(
                        monthlyIncome,
                        monthlyExpense,
                        monthlyBalance
                );

        BigDecimal balance =
                totalIncome.subtract(totalExpense);

        List<CategorySpendingContext> categorySpendings =
                transactionRepository
                        .findCategorySpendingByUserAndDateBetween(
                                user,
                                TransactionType.EXPENSE,
                                startDate,
                                endDate
                        )
                        .stream()
                        .map(projection ->
                                new CategorySpendingContext(
                                        projection.categoryName(),
                                        projection.spent()
                                )
                        )
                        .toList();

        List<BudgetEntity> activeBudgets =
                budgetRepository
                        .findAllByUserOrderByStartDateDesc(user)
                        .stream()
                        .filter(budget ->
                                !budget.getStartDate().isAfter(endDate)
                                        && !budget.getEndDate().isBefore(endDate)
                        )
                        .toList();

        Map<UUID, BigDecimal> spentByBudgetId =
                transactionRepository
                        .findBudgetSpentByUser(
                                user,
                                TransactionType.EXPENSE
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        projection -> projection.budgetId(),
                                        projection -> projection.spent()
                                )
                        );

        List<BudgetContext> budgets =
                activeBudgets
                        .stream()
                        .map(budget -> {

                            BigDecimal spent =
                                    spentByBudgetId.getOrDefault(
                                            budget.getId(),
                                            BigDecimal.ZERO
                                    );

                            BigDecimal remaining =
                                    budget.getAmount().subtract(spent);

                            int percentage =
                                    budget.getAmount().compareTo(BigDecimal.ZERO) == 0
                                            ? 0
                                            : spent
                                            .multiply(BigDecimal.valueOf(100))
                                            .divide(
                                                    budget.getAmount(),
                                                    0,
                                                    RoundingMode.HALF_UP
                                            )
                                            .intValue();

                            return new BudgetContext(
                                    budget.getCategory().getName(),
                                    budget.getAmount(),
                                    spent,
                                    remaining,
                                    percentage
                            );
                        })
                        .toList();

        return new FinancialInsightContext(
                totalIncome,
                totalExpense,
                balance,
                monthly,
                categorySpendings,
                budgets
        );
    }
}