package com.arkindustries.gogreen.api.request

import com.arkindustries.gogreen.api.response.AttachmentResponse

data class ProjectResponse(
    val project: Project
)

data class Project(
    val id: String,
    val title: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val attachments: List<AttachmentResponse>,
    val skills: List<String>,
    val feedback: String
)