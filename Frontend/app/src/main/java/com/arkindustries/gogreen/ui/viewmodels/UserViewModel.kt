package com.arkindustries.gogreen.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkindustries.gogreen.api.request.ForgetPasswordRequest
import com.arkindustries.gogreen.api.request.ResetPasswordRequest
import com.arkindustries.gogreen.api.request.SignupRequest
import com.arkindustries.gogreen.api.request.UpdateUserRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.LoginResponse
import com.arkindustries.gogreen.api.response.UserResponse
import com.arkindustries.gogreen.database.entites.UserEntity
import com.arkindustries.gogreen.ui.repositories.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> get() = _loadingState

    private val _allUsers = MutableLiveData<ApiResponse<List<UserResponse>>>()
    val allUsers: LiveData<ApiResponse<List<UserResponse>>> get() = _allUsers

    private val _userById = MutableLiveData<ApiResponse<UserResponse>>()
    val userById: LiveData<ApiResponse<UserResponse>> get() = _userById

    private val _updateUser = MutableLiveData<ApiResponse<UserResponse>>()
    val updateUser: LiveData<ApiResponse<UserResponse>> get() = _updateUser

    private val _deleteUser = MutableLiveData<ApiResponse<Unit>>()
    val deleteUser: LiveData<ApiResponse<Unit>> get() = _deleteUser

    private val _resetPasswordResult = MutableLiveData<ApiResponse<LoginResponse>>()
    val resetPasswordResult: LiveData<ApiResponse<LoginResponse>> get() = _resetPasswordResult

    private val _forgetPasswordResult = MutableLiveData<ApiResponse<String>>()
    val forgetPasswordResult: LiveData<ApiResponse<String>> get() = _forgetPasswordResult


    private val _loginResult = MutableLiveData<ApiResponse<LoginResponse>>()
    val loginResult: LiveData<ApiResponse<LoginResponse>> get() = _loginResult

    private val _signupApiResponse = MutableLiveData<ApiResponse<LoginResponse>>()
    val signupApiResponse: LiveData<ApiResponse<LoginResponse>> = _signupApiResponse

    private val _userByIdError = MutableLiveData<ApiResponse<*>>()
    val userByIdError: LiveData<ApiResponse<*>> get() = _userByIdError

    private val _updateUserError = MutableLiveData<ApiResponse<*>>()
    val updateUserError: LiveData<ApiResponse<*>> get() = _updateUserError

    private val _currentUserLoadingState = MutableLiveData<Boolean>()
    val currentUserLoadingState: LiveData<Boolean> get() = _currentUserLoadingState

    private val _currentUserResult = MutableLiveData<UserEntity>()
    val currentUserResult: LiveData<UserEntity> get() = _currentUserResult

    private val _currentUserError = MutableLiveData<ApiResponse<*>>()
    val currentUserError: LiveData<ApiResponse<*>> get() = _currentUserError

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

    fun currentUser() {
        _currentUserLoadingState.value = true
        viewModelScope.launch {
            val response = userRepository.getCurrentUser()

            if (response.status == "fail" || response.status == "error") {
                _currentUserError.value = response
            }

            val user = response.data?.user

            if (user != null) {
                _currentUserResult.value =
                    UserEntity(
                        user._id,
                        user.name,
                        user.email,
                        user.phoneNo,
                        user.userType,
                        user.image,
                        user.userStatus,
                        user.verified,
                        user.financeAllowed,
                        user.blocked.isBlocked,
                        user.blocked.reason,
                        user.joinedDate
                    )
            }

            _currentUserLoadingState.value = false
        }
    }

    fun getAllUsers() {
        _loadingState.value = true
        viewModelScope.launch {
            val response = userRepository.getAllUsers()
            _allUsers.value = response
            _loadingState.value = false
        }
    }

    fun getUserById(userId: String) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = userRepository.getUserById(userId)

            if (response.status == "fail" || response.status == "error") {
                _userByIdError.value = response
                _loadingState.value = false
                return@launch
            }

            _userById.value = response
            _loadingState.value = false
        }
    }

    fun updateUserById(userId: String, updateUserRequest: UpdateUserRequest) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = userRepository.updateUserById(userId, updateUserRequest)

            if (response.status == "fail" || response.status == "error") {
                _updateUserError.value = response
                _loadingState.value = false
                return@launch
            }

            _updateUser.postValue(response)
            _loadingState.value = false
        }
    }

    fun deleteUserById(userId: Long) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = userRepository.deleteUserById(userId)
            _deleteUser.postValue(response)
            _loadingState.value = false
        }
    }

    fun resetPassword(resetToken: String, resetPasswordRequest: ResetPasswordRequest) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = userRepository.resetPassword(resetToken, resetPasswordRequest)
            _resetPasswordResult.postValue(response)
            _loadingState.value = false
        }
    }

    fun forgetPassword(forgetPasswordRequest: ForgetPasswordRequest) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = userRepository.forgetPassword(forgetPasswordRequest)
            _forgetPasswordResult.postValue(response)
            _loadingState.value = false
        }
    }
}
