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
    suspend fun getCategories(): Response<ApiResponse<List<CategoryDto>>>

    @POST("categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): Response<ApiResponse<CategoryDto>>

    @PUT("categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: String,
        @Body request: CreateCategoryRequest
    ): Response<ApiResponse<CategoryDto>>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): Response<ApiResponse<Unit>>
}
