package com.finora.app.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// We define basic DTOs here for simplicity, typically they go in a separate file.
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val accessToken: String, val refreshToken: String)
data class RegisterRequest(val firstName: String, val lastName: String, val email: String, val password: String)
data class RefreshRequest(val refreshToken: String)
data class RefreshTokenResponse(val accessToken: String)

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<Unit>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @POST("auth/refresh")
    fun refreshTokenSync(@Body request: RefreshRequest): retrofit2.Call<ApiResponse<RefreshTokenResponse>>
}
