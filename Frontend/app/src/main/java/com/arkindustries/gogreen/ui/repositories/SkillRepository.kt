package com.arkindustries.gogreen.ui.repositories

import com.arkindustries.gogreen.api.request.CategoryRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.CategoryResponse
import com.arkindustries.gogreen.api.services.CategoryService
import com.arkindustries.gogreen.database.dao.CategoryDao
import com.arkindustries.gogreen.database.entites.CategoryEntity

class CategoryRepository(private val categoryDao: CategoryDao, private val categoryService: CategoryService) {

    suspend fun getCategoriesFromServer(): ApiResponse<List<CategoryResponse>> {
        return  categoryService.getCategories()
    }

    suspend fun createCategoryFromServer(categoryRequest: CategoryRequest): ApiResponse<CategoryResponse> {
        return categoryService.createCategory(categoryRequest);
    }

    suspend fun updateCategoryFromServer(
        categoryId: String,
        categoryRequest: CategoryRequest
    ): ApiResponse<CategoryResponse> {
        return categoryService.updateCategory(categoryId, categoryRequest)
    }

    suspend fun deleteCategoryFromServer(categoryId: String): ApiResponse<Unit> {
        return categoryService.deleteCategory(categoryId)
    }

    suspend fun getAllCategoriesFromLocal(): List<CategoryEntity> {
        return categoryDao.getAllCategories()
    }

    suspend fun getCategoryByIdFromLocal(categoryId: String): CategoryEntity {
        return categoryDao.getCategoryById(categoryId)
    }

    suspend fun upsertCategoriesToLocal(categories: List<CategoryEntity>) {
        categoryDao.upsertAll(categories)
    }

    suspend fun deleteAllCategoriesFromLocal() {
        categoryDao.deleteAll()
    }
}