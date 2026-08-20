package com.finora.app.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// We define basic DTOs here for simplicity, typically they go in a separate file.
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String, val refreshToken: String)
data class RegisterRequest(val firstName: String, val lastName: String, val email: String, val password: String)

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>
}
