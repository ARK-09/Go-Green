package com.arkindustries.gogreen.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkindustries.gogreen.api.response.GeocodingReverseResponse
import com.arkindustries.gogreen.api.response.GeocodingSearchResponse
import com.arkindustries.gogreen.ui.repositories.GeocodingRepository
import kotlinx.coroutines.launch

class GeocodingViewModel(private val geocodingRepository: GeocodingRepository) : ViewModel() {
    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> get() = _loadingState

    private val _geocodingSearch = MutableLiveData<List<GeocodingSearchResponse>>()
    val geocodingSearch: LiveData<List<GeocodingSearchResponse>> = _geocodingSearch

    private val _geocodingReverse = MutableLiveData<GeocodingReverseResponse> ()
    val geocodingReverse: LiveData<GeocodingReverseResponse> = _geocodingReverse

    private val _error = MutableLiveData<Exception>()
    val error: LiveData<Exception> = _error

    fun searchAddress(address: String) {
        _loadingState.value = true
        viewModelScope.launch {
            try{
                val response = geocodingRepository.searchAddress(address)
                _geocodingSearch.value = response
            } catch (exp: Exception) {
                _error.value = exp
            } finally {
                _loadingState.value = false
            }
        }
    }

    fun reverseGeocode(lat: String, lon: String) {
        _loadingState.value = true
        viewModelScope.launch {
            try{
                val response = geocodingRepository.reverseGeocode(lat, lon)
                _geocodingReverse.value = response
            } catch (exp: Exception) {
                _error.value = exp
            } finally {
                _loadingState.value = false
            }
        }
    }
}