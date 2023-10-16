package com.arkindustries.gogreen.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkindustries.gogreen.database.entites.CategoryEntity
import com.arkindustries.gogreen.ui.repositories.CategoryRepository
import kotlinx.coroutines.launch

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {

    init {
        refreshCategories()
    }

    fun refreshCategories () {
        viewModelScope.launch {
            val categories = repository.getCategoriesFromServer().data?.map { job ->
                return@map CategoryEntity(job._id, job.title)
            }

            if (categories != null) {
                repository.upsertCategoriesToLocal(categories)
            }
        }
    }

    suspend fun getAllCategories (): List<CategoryEntity> {
       return repository.getAllCategoriesFromLocal()
    }

    suspend fun getCategoryById(categoryId: String): CategoryEntity {
        return repository.getCategoryByIdFromLocal(categoryId)
    }

    suspend fun deleteAllCategories() {
        repository.deleteAllCategoriesFromLocal()
    }
}