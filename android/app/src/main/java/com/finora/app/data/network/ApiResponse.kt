package com.finora.app.data.network

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: ApiErrorResponse?,
    val validationErrors: Map<String, String>?,
    val timestamp: String?
)

data class ApiErrorResponse(
    val code: String?,
    val message: String?
)
