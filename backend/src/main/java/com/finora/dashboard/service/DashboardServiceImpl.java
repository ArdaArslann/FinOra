package com.finora.dashboard.service;

import com.finora.budget.entity.BudgetEntity;
import com.finora.budget.repository.BudgetRepository;
import com.finora.common.security.CurrentUserService;
import com.finora.dashboard.dto.BudgetUsageResponse;
import com.finora.dashboard.dto.DashboardResponse;
import com.finora.dashboard.dto.DashboardSummaryResponse;
import com.finora.dashboard.dto.RecentTransactionResponse;
import com.finora.dashboard.mapper.DashboardMapper;
import com.finora.dashboard.projection.BudgetSpentProjection;
import com.finora.transaction.enums.TransactionType;
import com.finora.transaction.repository.TransactionRepository;
import com.finora.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

        private final TransactionRepository transactionRepository;
        private final CurrentUserService currentUserService;
        private final DashboardMapper dashboardMapper;
        private final BudgetRepository budgetRepository;

        @Override
        public DashboardResponse getDashboard() {

                UserEntity user = currentUserService.getCurrentUser();

                BigDecimal totalIncome = transactionRepository.sumAmountByUserAndType(
                                user,
                                TransactionType.INCOME);

                BigDecimal totalExpense = transactionRepository.sumAmountByUserAndType(
                                user,
                                TransactionType.EXPENSE);

                BigDecimal balance = totalIncome.subtract(totalExpense);

                DashboardSummaryResponse summary = new DashboardSummaryResponse(
                                balance,
                                totalIncome,
                                totalExpense);
                List<RecentTransactionResponse> recentTransactions = transactionRepository
                                .findTop5ByUserOrderByTransactionDateDesc(user)
                                .stream()
                                .map(dashboardMapper::toRecentTransactionResponse)
                                .toList();

                List<BudgetEntity> budgets = budgetRepository
                                .findAllByUserOrderByStartDateDesc(user);

                List<BudgetSpentProjection> budgetSpentProjections = transactionRepository.findBudgetSpentByUser(
                                user,
                                TransactionType.EXPENSE);

                Map<UUID, BigDecimal> spentByBudgetId = budgetSpentProjections.stream()
                                .collect(
                                                Collectors.toMap(
                                                                BudgetSpentProjection::budgetId,
                                                                BudgetSpentProjection::spent));

                List<BudgetUsageResponse> budgetUsages = budgets.stream()
                                .map(budget -> {
                                        BigDecimal spent = spentByBudgetId.getOrDefault(
                                                        budget.getId(),
                                                        BigDecimal.ZERO);

                                        return toBudgetUsageResponse(
                                                        budget,
                                                        spent);
                                })
                                .toList();

                return new DashboardResponse(
                                summary,
                                budgetUsages,
                                recentTransactions,
                                null);
        }

        private BudgetUsageResponse toBudgetUsageResponse(
                        BudgetEntity budget,
                        BigDecimal spent) {

                BigDecimal remaining = budget.getAmount().subtract(spent);

                int percentage;

                if (budget.getAmount().compareTo(BigDecimal.ZERO) == 0) {
                        percentage = 0;
                } else {
                        percentage = spent
                                        .multiply(BigDecimal.valueOf(100))
                                        .divide(
                                                        budget.getAmount(),
                                                        0,
                                                        RoundingMode.HALF_UP)
                                        .intValue();
                }

                return new BudgetUsageResponse(
                                budget.getCategory().getId(),
                                budget.getCategory().getName(),
                                budget.getCategory().getIcon(),
                                budget.getCategory().getColor(),
                                budget.getAmount(),
                                spent,
                                remaining,
                                percentage);
        }

}
