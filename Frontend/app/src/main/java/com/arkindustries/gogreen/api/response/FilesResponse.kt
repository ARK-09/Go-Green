package com.arkindustries.gogreen.api.response

data class FilesResponse(
    val files: List<File>
)

data class File(
    val id: String,
    val mimeType: String,
    val originalName: String,
    val createdDate: String,
    val url: String?
)