package com.arkindustries.gogreen.api

import com.arkindustries.gogreen.api.request.LoginRequest
import com.arkindustries.gogreen.api.request.SignUpRequest
import com.arkindustries.gogreen.api.response.LoginResponse
import com.arkindustries.gogreen.api.response.Response
import com.arkindustries.gogreen.database.models.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("users/login")
    fun login(@Body request: LoginRequest): Response<LoginResponse>
    @POST("users/signin")
    fun signUp(@Body request: SignUpRequest): Response<LoginResponse>
    @GET("users/currentuser")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<User>
    @GET("users")
    suspend fun getUser(): Response<User>
}