package com.finora.dashboard.service;

import com.finora.common.security.CurrentUserService;
import com.finora.dashboard.dto.DashboardResponse;
import com.finora.dashboard.dto.DashboardSummaryResponse;
import com.finora.dashboard.dto.RecentTransactionResponse;
import com.finora.dashboard.mapper.DashboardMapper;
import com.finora.transaction.entity.TransactionEntity;
import com.finora.transaction.enums.TransactionType;
import com.finora.transaction.repository.TransactionRepository;
import com.finora.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;
    private final DashboardMapper dashboardMapper;

    @Override
    public DashboardResponse getDashboard() {

        UserEntity user = currentUserService.getCurrentUser();

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

        BigDecimal balance =
                totalIncome.subtract(totalExpense);

        DashboardSummaryResponse summary =
                new DashboardSummaryResponse(
                        balance,
                        totalIncome,
                        totalExpense
                );
        List<RecentTransactionResponse> recentTransactions =
                transactionRepository
                        .findTop5ByUserOrderByTransactionDateDesc(user)
                        .stream()
                        .map(dashboardMapper::toRecentTransactionResponse)
                        .toList();
        return new DashboardResponse(
                summary,
                List.of(),
                recentTransactions,
                null
        );
    }



}
