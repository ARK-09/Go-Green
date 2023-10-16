package com.arkindustries.gogreen.api.request

data class ReviewRequest(
    val feedback: String,
    val rating: Double
)