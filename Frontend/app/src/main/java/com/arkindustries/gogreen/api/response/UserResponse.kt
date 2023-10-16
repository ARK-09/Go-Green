package com.arkindustries.gogreen.api.response

data class UserResponse(val user: User)

data class User(
    val _id: String,
    val name: String,
    val email: String,
    val phoneNo: String = "",
    val userType: String = "",
    val image: Image,
    val userStatus: String,
    val verified: Boolean = false,
    val financeAllowed: Boolean = true,
    val blocked: BlockedInfo = BlockedInfo(false, ""),
    val joinedDate: String? = null
)

data class BlockedInfo(
    val isBlocked: Boolean,
    val reason: String?
)

data class Image(
    val id: String = "",
    val url: String = ""
)