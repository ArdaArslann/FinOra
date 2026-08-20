package com.finora.app.data.network

import retrofit2.Response
import retrofit2.http.GET

data class UserDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val createdAt: String
)

interface UserApi {
    @GET("users/me")
    suspend fun getCurrentUser(): Response<UserDto>
}
