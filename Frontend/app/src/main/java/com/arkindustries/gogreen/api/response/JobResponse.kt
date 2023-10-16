package com.arkindustries.gogreen.api.response

data class JobResponse(
    val job: Job
)

data class Job(
    val _id: String,
    val title: String,
    val description: String,
    val categories: List<Category>?,
    val skills: List<Skill>?,
    val budget: Double,
    val status: String,
    val expectedDuration: String,
    val paymentType: String,
    val attachments: List<AttachmentResponse>?,
    val user: User,
    val location: Location,
    val createdDate: String,
    val noOfProposals: Int = 0,
    val interviewing: Int = 0
)

data class Location(val type: String = "Point", val coordinates: List<String>? = null)