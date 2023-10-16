package com.arkindustries.gogreen.ui.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.arkindustries.gogreen.ui.repositories.CategoryRepository
import com.arkindustries.gogreen.ui.repositories.FileRepository
import com.arkindustries.gogreen.ui.repositories.JobRepository
import com.arkindustries.gogreen.ui.repositories.SkillRepository
import com.arkindustries.gogreen.ui.repositories.UserRepository
import com.arkindustries.gogreen.ui.viewmodels.JobViewModel

class JobViewModelFactory(private val jobRepository: JobRepository,
                          private val userRepository: UserRepository,
                          private val categoryRepository: CategoryRepository,
                          private val skillRepository: SkillRepository,
                          private val fileRepository: FileRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JobViewModel::class.java)) {
            return JobViewModel(jobRepository, userRepository, categoryRepository, skillRepository, fileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
