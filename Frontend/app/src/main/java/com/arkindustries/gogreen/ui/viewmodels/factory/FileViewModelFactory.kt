package com.arkindustries.gogreen.ui.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.arkindustries.gogreen.ui.repositories.FileRepository
import com.arkindustries.gogreen.ui.viewmodels.FileViewModel

class FileViewModelFactory(private val fileRepository: FileRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FileViewModel::class.java)) {
            return FileViewModel (fileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
