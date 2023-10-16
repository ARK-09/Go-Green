package com.arkindustries.gogreen.api.request

import com.arkindustries.gogreen.api.response.Language
import com.arkindustries.gogreen.api.response.Location

data class UpdateUserProfileRequest(
    val about: String? = null,
    val languages: List<Language>? = null,
    val dob: String? = null,
    val gender: String? = null,
    val address: String? = null,
    val location: Location? = null,
    val skills: List<String>? = null
)