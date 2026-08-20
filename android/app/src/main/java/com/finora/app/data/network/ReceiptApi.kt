package com.finora.app.data.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

data class ReceiptExtractionDto(
    val merchantName: String,
    val totalAmount: Double,
    val transactionDate: String,
    val currency: String,
    val suggestedCategory: String
)

data class ReceiptDto(
    val id: String,
    val originalFileName: String,
    val status: String, // UPLOADED, PROCESSING, PROCESSED, FAILED
    val extraction: ReceiptExtractionDto?
)

data class ConfirmReceiptRequest(
    val amount: Double,
    val description: String,
    val transactionDate: String,
    val categoryId: String
)

interface ReceiptApi {
    @Multipart
    @POST("receipts/upload")
    suspend fun uploadReceipt(@Part file: MultipartBody.Part): Response<ReceiptDto>

    @POST("receipts/{id}/confirm")
    suspend fun confirmReceipt(
        @Path("id") id: String,
        @Body request: ConfirmReceiptRequest
    ): Response<Unit>
}
