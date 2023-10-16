package com.arkindustries.gogreen.api.response


data class UserProfileResponse(
    val profile: Profile
)

data class Profile(
    val _id: String,
    val about: String,
    val languages: List<Language>,
    val dob: String,
    val gender: String? = null,
    val rating: Float,
    val ranking: String,
    val address: String,
    val location: Location,
    val skills: List<Skill>,
    val projects: List<Project>,
    val user: User
)

data class Language(
    val _id: String?,
    val name: String,
    val experience: String
)