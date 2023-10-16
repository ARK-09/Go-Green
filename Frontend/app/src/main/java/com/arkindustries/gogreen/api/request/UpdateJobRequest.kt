package com.arkindustries.gogreen.api.request

data class UpdateJobRequest(
    val title: String?,
    val description: String?,
    val categories: List<String>?,
    val budget: Double?,
    val status: String?,
    val expectedDuration: String?,
    val paymentType: String?
)
