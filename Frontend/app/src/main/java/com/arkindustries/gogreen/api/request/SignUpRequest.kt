package com.arkindustries.gogreen.api.request

import com.google.gson.annotations.SerializedName

data class SignUpRequest (
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("userType")
    val userType: String
)