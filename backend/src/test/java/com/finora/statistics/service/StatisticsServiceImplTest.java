package com.finora.statistics.service;

import com.finora.budget.repository.BudgetRepository;
import com.finora.common.security.CurrentUserService;
import com.finora.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.finora.common.exception.BusinessException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    @Test
    void getStatistics_ShouldThrowException_WhenDatesAreInvalid() {
        assertThrows(BusinessException.class, () -> {
            statisticsService.getStatistics(null, null);
        });

        assertThrows(BusinessException.class, () -> {
            statisticsService.getStatistics(LocalDate.of(2024, 1, 2), LocalDate.of(2024, 1, 1));
        });
    }
}
