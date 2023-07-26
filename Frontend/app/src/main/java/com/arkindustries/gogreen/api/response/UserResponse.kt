package com.arkindustries.gogreen.api.response

data class UserResponse(val user: User)

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phoneNo: String,
    val userType: String,
    val image: String,
    val userStatus: String,
    val verified: Boolean,
    val financeAllowed: Boolean,
    val blocked: BlockedInfo
)

data class BlockedInfo(
    val isBlocked: Boolean,
    val reason: String?
)