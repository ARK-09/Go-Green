package com.arkindustries.gogreen.api.request

data class CreateProjectRequest(
    val title: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val attachments: List<AttachmentRequest>,
    val skills: List<String>,
    val contractId: String
)