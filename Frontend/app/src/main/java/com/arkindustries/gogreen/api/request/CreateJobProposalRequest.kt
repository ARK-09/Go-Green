package com.arkindustries.gogreen.api.request

data class CreateJobProposalRequest(
    val bidAmount: Double,
    val coverLetter: String,
    val proposedDuration: String,
    val attachments: List<AttachmentRequest>?
)