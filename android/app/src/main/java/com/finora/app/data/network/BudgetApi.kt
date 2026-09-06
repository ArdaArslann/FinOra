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
    val createdAt: String?, // made nullable as backend doesn't send it in BudgetResponse
    val spent: Double? = 0.0
)

data class CreateBudgetRequest(
    val amount: Double,
    val period: String,
    val startDate: String,
    val endDate: String,
    val categoryId: String
)

data class UpdateBudgetRequest(
    val amount: Double,
    val period: String,
    val startDate: String,
    val endDate: String,
    val categoryId: String
)

interface BudgetApi {
    @GET("budgets")
    suspend fun getBudgets(): Response<ApiResponse<List<BudgetDto>>>

    @POST("budgets")
    suspend fun createBudget(@Body request: CreateBudgetRequest): Response<ApiResponse<BudgetDto>>

    @PUT("budgets/{id}")
    suspend fun updateBudget(
        @Path("id") id: String,
        @Body request: UpdateBudgetRequest
    ): Response<ApiResponse<BudgetDto>>

    @DELETE("budgets/{id}")
    suspend fun deleteBudget(@Path("id") id: String): Response<ApiResponse<Unit>>
}
