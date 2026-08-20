package com.finora.app.data.network

import retrofit2.Response
import retrofit2.http.*

data class BudgetDto(
    val id: String,
    val amount: Double,
    val period: String, // DAILY, WEEKLY, MONTHLY, YEARLY
    val startDate: String,
    val endDate: String,
    val categoryId: String,
    val createdAt: String
)

data class CreateBudgetRequest(
    val amount: Double,
    val period: String,
    val categoryId: String
)

interface BudgetApi {
    @GET("budgets")
    suspend fun getBudgets(): Response<List<BudgetDto>>

    @POST("budgets")
    suspend fun createBudget(@Body request: CreateBudgetRequest): Response<BudgetDto>
}
