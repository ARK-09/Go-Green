package com.arkindustries.gogreen.api.response

data class Response<T>(
    val status: String,
    val length: Int? = null,
    val data: T? = null,
    val message: String? = null,
)
