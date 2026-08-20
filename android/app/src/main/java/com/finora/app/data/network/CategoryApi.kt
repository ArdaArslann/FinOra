package com.finora.app.data.network

import retrofit2.Response
import retrofit2.http.*

data class CategoryDto(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val createdAt: String
)

data class CreateCategoryRequest(
    val name: String,
    val icon: String,
    val color: String
)

interface CategoryApi {
    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @POST("categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): Response<CategoryDto>
}
