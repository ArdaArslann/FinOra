package com.finora.app.data.network

import com.google.gson.Gson
import retrofit2.Response

fun <T> Response<ApiResponse<T>>.getErrorMessage(): String? {
    return try {
        val errorJson = errorBody()?.string()
        if (!errorJson.isNullOrEmpty()) {
            val apiResponse = Gson().fromJson(errorJson, ApiResponse::class.java)
            if (!apiResponse.validationErrors.isNullOrEmpty()) {
                apiResponse.validationErrors.values.joinToString("\n")
            } else {
                apiResponse.error?.message
            }
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}
