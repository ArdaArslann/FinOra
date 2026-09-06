package com.finora.app.data.network

import retrofit2.Response
import retrofit2.http.*

data class TransactionDto(
    val id: String,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val description: String,
    val transactionDate: String,
    val categoryId: String,
    val createdAt: String
)

data class CreateTransactionRequest(
    val amount: Double,
    val type: String,
    val description: String,
    val transactionDate: String,
    val categoryId: String
)

interface TransactionApi {
    @GET("transactions")
    suspend fun getTransactions(): Response<ApiResponse<List<TransactionDto>>>

    @POST("transactions")
    suspend fun createTransaction(@Body request: CreateTransactionRequest): Response<ApiResponse<TransactionDto>>

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: String): Response<ApiResponse<Unit>>

    @PUT("transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: String,
        @Body request: CreateTransactionRequest
    ): Response<ApiResponse<TransactionDto>>
}
