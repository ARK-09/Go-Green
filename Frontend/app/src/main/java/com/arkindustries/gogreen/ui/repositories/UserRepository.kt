package com.arkindustries.gogreen.ui.repositories

import com.arkindustries.gogreen.api.request.ForgetPasswordRequest
import com.arkindustries.gogreen.api.request.LoginRequest
import com.arkindustries.gogreen.api.request.ResetPasswordRequest
import com.arkindustries.gogreen.api.request.SignupRequest
import com.arkindustries.gogreen.api.request.UpdateUserRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.LoginResponse
import com.arkindustries.gogreen.api.response.UserResponse
import com.arkindustries.gogreen.api.services.UserService
import com.arkindustries.gogreen.database.dao.UserDao
import com.arkindustries.gogreen.database.entites.UserEntity
import com.arkindustries.gogreen.utils.handleApiCall

class UserRepository(private val userService: UserService, private val userDao: UserDao) {
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
        return handleApiCall {
            userService.getAllUsers()
        }
    }

    suspend fun getUserById(userId: String): ApiResponse<UserResponse> {
        return handleApiCall { userService.getUserById(userId) }
    }

    suspend fun updateUserById(
        userId: String,
        updateUserRequest: UpdateUserRequest
    ): ApiResponse<UserResponse> {
        return handleApiCall { userService.updateUserById(userId, updateUserRequest) }
    }

    suspend fun deleteUserById(userId: Long): ApiResponse<Unit> {
        return handleApiCall { userService.deleteUserById(userId) }
    }

    suspend fun resetPassword(
        resetToken: String,
        resetPasswordRequest: ResetPasswordRequest
    ): ApiResponse<LoginResponse> {
        return handleApiCall { userService.resetPassword(resetToken, resetPasswordRequest) }
    }

    suspend fun forgetPassword(forgetPasswordRequest: ForgetPasswordRequest): ApiResponse<String> {
        return handleApiCall { userService.forgetPassword(forgetPasswordRequest) }
    }

    suspend fun getUserByIdFromLocal(userId: String): UserEntity {
        return userDao.getUserById(userId)
    }

    suspend fun getAllUsersFromLocal(): List<UserEntity> {
        return userDao.getAllUsers()
    }

    suspend fun upsertUsersToLocal(users: List<UserEntity>) {
        userDao.upsertUser(users)
    }

    suspend fun deleteUserFromLocal(userId: String) {
        userDao.deleteById(userId)
    }

    suspend fun deleteAllUsersFromLocal() {
        userDao.deleteAllUsers()
    }
}

