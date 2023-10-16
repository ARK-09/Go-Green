package com.arkindustries.gogreen.api.request

data class UserProfileResponse(
    val profile: Profile
)

data class Profile(
    val id: String,
    val about: String,
    val languages: List<String>,
    val dob: String,
    val gender: String,
    val ranking: Int,
    val address: String,
    val location: String,
    val skills: List<String>
)