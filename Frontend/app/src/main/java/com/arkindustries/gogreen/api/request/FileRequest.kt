package com.arkindustries.gogreen.api.request

import okhttp3.MultipartBody

data class AttachmentRequest(
    val files: List<MultipartBody.Part>
)