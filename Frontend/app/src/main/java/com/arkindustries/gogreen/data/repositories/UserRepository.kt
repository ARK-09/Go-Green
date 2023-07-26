package com.arkindustries.gogreen.data.repositories

import android.content.Context
import com.arkindustries.gogreen.api.interceptor.HttpClientProvider
import com.arkindustries.gogreen.api.request.ForgetPasswordRequest
import com.arkindustries.gogreen.api.request.LoginRequest
import com.arkindustries.gogreen.api.request.ResetPasswordRequest
import com.arkindustries.gogreen.api.request.SignupRequest
import com.arkindustries.gogreen.api.request.UpdateUserRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.LoginResponse
import com.arkindustries.gogreen.api.response.UserResponse
import com.arkindustries.gogreen.api.services.UserService
import com.arkindustries.gogreen.utils.handleApiCall

class UserRepository(private val context: Context) {
    private val userService = HttpClientProvider.createService(UserService::class.java, context)

    suspend fun login(email: String, password: String): ApiResponse<LoginResponse> {
        return handleApiCall {
            userService.login(LoginRequest(email, password))
        }
    }

    suspend fun signup(signupData: SignupRequest): ApiResponse<LoginResponse> {
        return handleApiCall {
            userService.signup(signupData)
        }
    }

    suspend fun getCurrentUser(): ApiResponse<UserResponse> {
        return handleApiCall {
            userService.getCurrentUser()
        }
    }

    suspend fun getAllUsers(): ApiResponse<List<UserResponse>> {
        // Call the API using Retrofit
        return handleApiCall {
            userService.getAllUsers()
        }
    }

    suspend fun getUserById(userId: Long): ApiResponse<UserResponse> {
        // Call the API using Retrofit
        return handleApiCall { userService.getUserById(userId) }
    }

    suspend fun updateUserById(
        userId: Long,
        updateUserRequest: UpdateUserRequest
    ): ApiResponse<UserResponse> {
        // Call the API using Retrofit
        return handleApiCall { userService.updateUserById(userId, updateUserRequest) }
    }

    suspend fun deleteUserById(userId: Long): ApiResponse<Unit> {
        // Call the API using Retrofit
        return handleApiCall { userService.deleteUserById(userId) }
    }

    suspend fun resetPassword(
        resetToken: String,
        resetPasswordRequest: ResetPasswordRequest
    ): ApiResponse<Unit> {
        // Call the API using Retrofit
        return handleApiCall { userService.resetPassword(resetToken, resetPasswordRequest) }
    }

    suspend fun forgetPassword(forgetPasswordRequest: ForgetPasswordRequest): ApiResponse<Unit> {
        // Call the API using Retrofit
        return handleApiCall { userService.forgetPassword(forgetPasswordRequest) }
    }
}

