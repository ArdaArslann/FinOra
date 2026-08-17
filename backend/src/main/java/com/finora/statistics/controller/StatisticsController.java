package com.finora.statistics.controller;

import com.finora.common.dto.ApiResponse;
import com.finora.statistics.dto.StatisticsResponse;
import com.finora.statistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Statistics & Reporting API")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping
    @Operation(summary = "Get user statistics for a date range")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getStatistics(
            @Parameter(description = "Start date (inclusive, YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (inclusive, YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        StatisticsResponse response = statisticsService.getStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
