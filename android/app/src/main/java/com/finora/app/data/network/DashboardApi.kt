package com.finora.app.data.network

import retrofit2.Response
import retrofit2.http.GET

data class DashboardSummaryResponse(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double
)

data class DashboardResponseDto(
    val summary: DashboardSummaryResponse
)

data class BudgetInsightDto(
    val category: String,
    val spent: Double,
    val budget: Double,
    val remaining: Double,
    val usagePercentage: Double
)

data class FinancialInsightResponse(
    val summary: String,
    val budgetInsights: List<BudgetInsightDto>,
    val recommendations: List<String>
)

interface DashboardApi {
    @GET("dashboard")
    suspend fun getSummary(): Response<ApiResponse<DashboardResponseDto>>

    @GET("dashboard/insight")
    suspend fun getAiInsights(): Response<ApiResponse<FinancialInsightResponse>>
}
