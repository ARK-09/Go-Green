package com.arkindustries.gogreen.api.response

data class AttachmentResponse(
    val id: String,
    val mimeType: String,
    val originalName: String,
    val url: String? = null,
    val createdDate: String
)