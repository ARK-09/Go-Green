package com.arkindustries.gogreen.api.response

data class ReviewsResponse(
    val reviews: List<Review>
)

data class Review(
    val doc: Job,
    val clientFeedback: String,
    val clientRating: Double,
)