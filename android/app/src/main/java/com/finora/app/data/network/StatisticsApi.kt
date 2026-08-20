package com.finora.app.data.network

import retrofit2.Response
import retrofit2.http.GET

data class MonthlyStatisticDto(
    val month: String,
    val income: Double,
    val expense: Double
)

data class CategoryStatisticDto(
    val categoryName: String,
    val totalAmount: Double,
    val percentage: Double
)

data class StatisticsResponse(
    val monthlyStats: List<MonthlyStatisticDto>,
    val categoryStats: List<CategoryStatisticDto>
)

interface StatisticsApi {
    @GET("statistics")
    suspend fun getStatistics(): Response<ApiResponse<StatisticsResponse>>
}
