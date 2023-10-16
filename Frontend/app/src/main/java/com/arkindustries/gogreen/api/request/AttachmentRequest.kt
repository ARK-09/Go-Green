package com.arkindustries.gogreen.api.request

data class AttachmentRequest(
    val id: String,
    val mimeType: String,
    val originalName: String,
    val createdDate: String,
)