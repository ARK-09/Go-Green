package com.arkindustries.gogreen.ui.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.arkindustries.gogreen.ui.repositories.SkillRepository
import com.arkindustries.gogreen.ui.viewmodels.SkillViewModel

class SkillViewModelFactory(private val skillRepository: SkillRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SkillViewModel::class.java)) {
            return SkillViewModel (skillRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
