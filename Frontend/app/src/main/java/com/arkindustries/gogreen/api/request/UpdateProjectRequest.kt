package com.arkindustries.gogreen.api.request

data class UpdateProjectRequest(
    val title: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val skills: List<String>,
    val contractId: String
)