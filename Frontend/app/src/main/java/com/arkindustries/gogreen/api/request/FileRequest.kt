package com.arkindustries.gogreen.api.request

import okhttp3.MultipartBody

data class FileRequest(
    val files: List<MultipartBody.Part>
)