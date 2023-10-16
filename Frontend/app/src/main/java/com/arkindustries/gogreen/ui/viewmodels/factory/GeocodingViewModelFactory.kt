package com.arkindustries.gogreen.ui.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.arkindustries.gogreen.ui.repositories.GeocodingRepository
import com.arkindustries.gogreen.ui.viewmodels.GeocodingViewModel

class GeocodingViewModelFactory(
    private val geocodingRepository: GeocodingRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GeocodingViewModel::class.java)) {
            return GeocodingViewModel(
                geocodingRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
