package com.arkindustries.gogreen.ui.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.arkindustries.gogreen.ui.repositories.CategoryRepository
import com.arkindustries.gogreen.ui.viewmodels.CategoryViewModel

class CategoryViewModelFactory(private val categoryRepository: CategoryRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            return CategoryViewModel (categoryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
