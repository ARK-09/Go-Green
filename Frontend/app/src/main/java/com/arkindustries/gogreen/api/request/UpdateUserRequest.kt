package com.arkindustries.gogreen.api.request

data class UpdateUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val phoneNo: String,
    val image: String
)