package com.arkindustries.gogreen.ui.repositories

import com.arkindustries.gogreen.api.request.CategoryRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.CategoriesResponse
import com.arkindustries.gogreen.api.response.CategoryResponse
import com.arkindustries.gogreen.api.services.CategoryService
import com.arkindustries.gogreen.database.dao.CategoryDao
import com.arkindustries.gogreen.database.entites.CategoryEntity
import com.arkindustries.gogreen.utils.handleApiCall

class CategoryRepository(
    private val categoryService: CategoryService,
    private val categoryDao: CategoryDao
) {

    suspend fun getCategoriesFromServer(): ApiResponse<CategoriesResponse> {
        return handleApiCall {
            categoryService.getCategories()
        }
    }

    suspend fun createCategoryFromServer(categoryRequest: CategoryRequest): ApiResponse<CategoryResponse> {
        return handleApiCall {
            categoryService.createCategory(categoryRequest);
        }
    }

    suspend fun updateCategoryAtServer(
        categoryId: String,
        categoryRequest: CategoryRequest
    ): ApiResponse<CategoryResponse> {
        return handleApiCall {
            categoryService.updateCategory(categoryId, categoryRequest)
        }
    }

    suspend fun deleteCategoryFromServer(categoryId: String): ApiResponse<Unit> {
        return handleApiCall {
            categoryService.deleteCategory(categoryId)
        }
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