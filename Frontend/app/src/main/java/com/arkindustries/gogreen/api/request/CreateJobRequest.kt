package com.arkindustries.gogreen.api.request

data class CreateJobRequest(
    val title: String,
    val description: String,
    val categories: List<String>,
    val skills: List<String>,
    val budget: Double,
    val expectedDuration: String,
    val paymentType: String,
    var attachments: List<AttachmentRequest>?,
    val location: LocationRequest
)