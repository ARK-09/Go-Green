package com.arkindustries.gogreen.api.services

import com.arkindustries.gogreen.api.request.ForgetPasswordRequest
import com.arkindustries.gogreen.api.request.LoginRequest
import com.arkindustries.gogreen.api.request.ResetPasswordRequest
import com.arkindustries.gogreen.api.request.SignupRequest
import com.arkindustries.gogreen.api.request.UpdateUserRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.LoginResponse
import com.arkindustries.gogreen.api.response.UserResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface UserService {
    @POST("users/login")
    suspend fun login(@Body loginRequest: LoginRequest): ApiResponse<LoginResponse>

    @POST("users/signup")
    suspend fun signup(@Body signupRequest: SignupRequest): ApiResponse<LoginResponse>

    @GET("users/currentUser")
    suspend fun getCurrentUser(): ApiResponse<UserResponse>

    @GET("users")
    suspend fun getAllUsers(): ApiResponse<List<UserResponse>>

    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") userId: Long
    ): ApiResponse<UserResponse>

    @PATCH("users/{id}")
    suspend fun updateUserById(
        @Path("id") userId: Long,
        @Body updateUserRequest: UpdateUserRequest
    ): ApiResponse<UserResponse>

    @DELETE("users/{id}")
    suspend fun deleteUserById(
        @Path("id") userId: Long
    ): ApiResponse<Unit>

    @POST("users/resetpassword/{resettokken}")
    suspend fun resetPassword(
        @Path("resettokken") resetToken: String,
        @Body resetPasswordRequest: ResetPasswordRequest
    ): ApiResponse<Unit>

    @POST("users/forgetpassword")
    suspend fun forgetPassword(@Body forgetPasswordRequest: ForgetPasswordRequest): ApiResponse<Unit>
}
