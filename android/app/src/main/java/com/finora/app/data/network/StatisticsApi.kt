package com.finora.app.data.network

import retrofit2.Response
import retrofit2.http.GET
import com.google.gson.annotations.SerializedName

data class MonthlyStatisticDto(
    val month: String,
    val income: Double,
    val expense: Double
)

data class CategoryStatisticDto(
    val categoryName: String,
    @SerializedName("amount") val totalAmount: Double,
    val percentage: Double
)

data class StatisticsResponse(
    @SerializedName("monthlyTrend") val monthlyStats: List<MonthlyStatisticDto>?,
    @SerializedName("categoryBreakdown") val categoryStats: List<CategoryStatisticDto>?
)

interface StatisticsApi {
    @GET("statistics")
    suspend fun getStatistics(
        @retrofit2.http.Query("startDate") startDate: String,
        @retrofit2.http.Query("endDate") endDate: String
    ): Response<ApiResponse<StatisticsResponse>>
}
