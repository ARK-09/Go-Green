package com.arkindustries.gogreen.data.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkindustries.gogreen.api.request.SignupRequest
import com.arkindustries.gogreen.api.response.LoginResponse
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.UserResponse
import com.arkindustries.gogreen.data.repositories.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> get() = _loadingState

    private val _loginResult = MutableLiveData<ApiResponse<LoginResponse>>()
    val loginResult: LiveData<ApiResponse<LoginResponse>> get() = _loginResult

    private val _signupApiResponse = MutableLiveData<ApiResponse<LoginResponse>>()
    val signupApiResponse: LiveData<ApiResponse<LoginResponse>> = _signupApiResponse

    private val _currentUserResult = MutableLiveData<ApiResponse<UserResponse>>()
    val currentUserResult: LiveData<ApiResponse<UserResponse>> get() = _currentUserResult

    fun login(email: String, password: String) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = userRepository.login(email, password)
            _loginResult.postValue(response)
            _loadingState.value = false
        }
    }

    fun signup(signupRequest: SignupRequest) {
        _loadingState.value = true
        viewModelScope.launch {
            _signupApiResponse.value = userRepository.signup(signupRequest)
            _loadingState.value = false
        }
    }

    fun currentUser () {
        _loadingState.value = true
        viewModelScope.launch {
            _currentUserResult.value = userRepository.getCurrentUser()
            _loadingState.value = false
        }
    }
}
