package com.arkindustries.gogreen.api.request

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val userType: String,
    val phoneNo: String?,
    val image: String?
)