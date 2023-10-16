package com.arkindustries.gogreen.api.response

data class ProposalResponse(
    val proposal: Proposal
)

data class Proposal(
    val _id: String,
    val doc: String,
    val user: User,
    val bidAmount: Double,
    val status: String,
    val coverLetter: String,
    val proposedDuration: String,
    val attachments: List<AttachmentResponse>?,
    val clientFeedback: String?,
    val talentFeedback: String?,
    val clientRating: Double?,
    val talentRating: Double?,
    val type: String,
    val job: Job?
)