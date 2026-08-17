package com.finora.statistics.service;

import com.finora.statistics.dto.StatisticsResponse;

import java.time.LocalDate;

public interface StatisticsService {
    StatisticsResponse getStatistics(LocalDate startDate, LocalDate endDate);
}
