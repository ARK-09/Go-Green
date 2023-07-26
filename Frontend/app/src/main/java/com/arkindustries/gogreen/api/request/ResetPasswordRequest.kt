package com.arkindustries.gogreen.api.request

data class ResetPasswordRequest(
   val email: String,
   val newPassword: String
)