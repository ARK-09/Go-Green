package com.arkindustries.gogreen.api.services

import com.arkindustries.gogreen.api.request.CategoryRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.CategoriesResponse
import com.arkindustries.gogreen.api.response.CategoryResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CategoryService {
    @GET("categories")
    suspend fun getCategories(): ApiResponse<CategoriesResponse>

    @POST("categories")
    suspend fun createCategory(@Body request: CategoryRequest): ApiResponse<CategoryResponse>

    @PATCH("categories/{id}")
    suspend fun updateCategory(
        @Path("id") categoryId: String,
        @Body request: CategoryRequest
    ): ApiResponse<CategoryResponse>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") categoryId: String): ApiResponse<Unit>
}
