package com.finora.statistics.service;

import com.finora.budget.entity.BudgetEntity;
import com.finora.budget.repository.BudgetRepository;
import com.finora.common.security.CurrentUserService;
import com.finora.dashboard.projection.BudgetSpentProjection;
import com.finora.dashboard.projection.CategorySpentProjection;
import com.finora.statistics.dto.BudgetPerformance;
import com.finora.statistics.dto.StatisticsResponse;
import com.finora.statistics.projection.CategoryStatistics;
import com.finora.statistics.projection.DailyStatistics;
import com.finora.statistics.projection.MonthlyStatistics;
import com.finora.common.exception.BusinessException;
import com.finora.transaction.enums.TransactionType;
import com.finora.transaction.repository.TransactionRepository;
import com.finora.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional(readOnly = true)
    public StatisticsResponse getStatistics(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException("INVALID_DATE_RANGE", "startDate and endDate are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("INVALID_DATE_RANGE", "startDate cannot be after endDate");
        }

        UserEntity user = currentUserService.getCurrentUser();

        BigDecimal totalIncome = transactionRepository.sumAmountByUserAndTypeAndTransactionDateBetween(user, TransactionType.INCOME, startDate, endDate);
        BigDecimal totalExpense = transactionRepository.sumAmountByUserAndTypeAndTransactionDateBetween(user, TransactionType.EXPENSE, startDate, endDate);
        
        long transactionCount = transactionRepository.countByUserAndTransactionDateBetween(user, startDate, endDate);
        
        List<DailyStatistics> dailyTrend = transactionRepository.getDailyTrend(user, startDate, endDate);
        List<MonthlyStatistics> monthlyTrend = transactionRepository.getMonthlyTrend(user, startDate, endDate);

        List<CategorySpentProjection> expenseCategories = transactionRepository.findCategorySpendingByUserAndDateBetween(user, TransactionType.EXPENSE, startDate, endDate);
        
        List<CategoryStatistics> categoryBreakdown = expenseCategories.stream()
                .map(p -> {
                    BigDecimal percentage = BigDecimal.ZERO;
                    if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
                        percentage = p.spent().divide(totalExpense, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                    }
                    return new CategoryStatistics(p.categoryId(), p.categoryName(), p.spent(), percentage);
                })
                .collect(Collectors.toList());

        List<BudgetEntity> budgets = budgetRepository.findAllByUserOrderByStartDateDesc(user);
        List<BudgetSpentProjection> budgetSpent = transactionRepository.findBudgetSpentByUser(user, TransactionType.EXPENSE);
        Map<java.util.UUID, BigDecimal> spentMap = budgetSpent.stream()
                .collect(Collectors.toMap(BudgetSpentProjection::budgetId, BudgetSpentProjection::spent));

        List<BudgetPerformance> budgetPerformance = budgets.stream()
                .filter(b -> !b.getEndDate().isBefore(startDate) && !b.getStartDate().isAfter(endDate))
                .map(b -> {
                    BigDecimal amountSpent = spentMap.getOrDefault(b.getId(), BigDecimal.ZERO);
                    BigDecimal remainingAmount = b.getAmount().subtract(amountSpent);
                    BigDecimal usagePercentage = BigDecimal.ZERO;
                    if (b.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                        usagePercentage = amountSpent.divide(b.getAmount(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                    }
                    String status = "NORMAL";
                    if (usagePercentage.compareTo(new BigDecimal("100")) >= 0) {
                        status = "EXCEEDED";
                    } else if (usagePercentage.compareTo(new BigDecimal("80")) >= 0) {
                        status = "WARNING";
                    }

                    return new BudgetPerformance(
                            b.getCategory().getId(),
                            b.getCategory().getName(),
                            b.getAmount(),
                            amountSpent,
                            remainingAmount,
                            usagePercentage,
                            status
                    );
                })
                .collect(Collectors.toList());

        BigDecimal balance = totalIncome.subtract(totalExpense);

        return new StatisticsResponse(
                startDate,
                endDate,
                totalIncome,
                totalExpense,
                balance,
                transactionCount,
                categoryBreakdown,
                dailyTrend,
                monthlyTrend,
                budgetPerformance
        );
    }
}
