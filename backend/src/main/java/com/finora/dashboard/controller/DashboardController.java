package com.finora.dashboard.controller;

import com.finora.common.dto.ApiResponse;
import com.finora.common.security.CurrentUserService;
import com.finora.dashboard.ai.response.FinancialInsightResponse;
import com.finora.dashboard.ai.service.FinancialInsightService;
import com.finora.dashboard.dto.DashboardResponse;
import com.finora.dashboard.service.DashboardService;
import com.finora.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final FinancialInsightService financialInsightService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {

        DashboardResponse response =
                dashboardService.getDashboard();

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @GetMapping("/insight")
    public ResponseEntity<ApiResponse<FinancialInsightResponse>> getInsight() {

        UserEntity user =
                currentUserService.getCurrentUser();

        FinancialInsightResponse insight =
                financialInsightService.generateInsight(user);

        return ResponseEntity.ok(
                ApiResponse.success(insight)
        );
    }
}