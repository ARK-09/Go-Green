package com.arkindustries.gogreen.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkindustries.gogreen.api.request.CategoryRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.database.entites.CategoryEntity
import com.arkindustries.gogreen.ui.repositories.CategoryRepository
import kotlinx.coroutines.launch

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {
    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> get() = _loadingState

    private val _categories = MutableLiveData<List<CategoryEntity>>()
    val categories: LiveData<List<CategoryEntity>> = _categories

    private val _category = MutableLiveData<CategoryEntity>()
    val category: LiveData<CategoryEntity> = _category

    private val _error = MutableLiveData<ApiResponse<*>>()
    val error: LiveData<ApiResponse<*>> = _error

    init {
        refreshCategories()
    }

    fun refreshCategories() {
        _loadingState.value = true
        viewModelScope.launch {
            val response = repository.getCategoriesFromServer()

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loadingState.value = false
                return@launch
            }

            val categories = response.data?.categories?.map { category ->
                return@map CategoryEntity(category._id, category.title)
            }

            if (categories != null) {
                repository.upsertCategoriesToLocal(categories)
                getAllCategories()
            }

            _loadingState.value = false
        }
    }

    fun getAllCategories() {
        _loadingState.value = true
        viewModelScope.launch {
            _categories.value = repository.getAllCategoriesFromLocal()
            _loadingState.value = false
        }
    }

    fun getCategoryById(categoryId: String) {
        _loadingState.value = true
        viewModelScope.launch {
            _category.value = repository.getCategoryByIdFromLocal(categoryId)
            _loadingState.value = false
        }
    }

    fun deleteAllCategories() {
        _loadingState.value = true
        viewModelScope.launch {
            repository.deleteAllCategoriesFromLocal()
            _loadingState.value = false
        }
    }

    fun createCategoryAtServer(categoryRequest: CategoryRequest) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = repository.createCategoryFromServer(categoryRequest)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loadingState.value = false
                return@launch
            }

            val categories = mutableListOf<CategoryEntity>()
            response.data?.let {
                categories.add(CategoryEntity(it.category._id, response.data.category.title))
            }

            repository.upsertCategoriesToLocal(categories)
            _loadingState.value = false
        }
    }

    fun updateCategoryAtServer(categoryId: String, categoryRequest: CategoryRequest) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = repository.updateCategoryAtServer(categoryId, categoryRequest)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loadingState.value = false
                return@launch
            }

            if (response.data != null) {
                repository.upsertCategoriesToLocal(
                    mutableListOf(
                        CategoryEntity(response.data.category._id, response.data.category.title)
                    )
                )
                _category.value = repository.getCategoryByIdFromLocal(categoryId)
            }
            _loadingState.value = false
        }
    }

    fun deleteCategoryFromServer(categoryId: String) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = repository.deleteCategoryFromServer(categoryId)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loadingState.value = false
                return@launch
            }

            repository.deleteCategoryFromServer(categoryId)
            _loadingState.value = false
        }
    }
}