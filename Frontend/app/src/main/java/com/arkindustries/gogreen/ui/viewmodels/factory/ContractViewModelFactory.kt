package com.arkindustries.gogreen.ui.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.arkindustries.gogreen.ui.repositories.ContractRepository
import com.arkindustries.gogreen.ui.viewmodels.ContractViewModel

class ContractViewModelFactory(private val contractRepository: ContractRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContractViewModel::class.java)) {
            return ContractViewModel (contractRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
