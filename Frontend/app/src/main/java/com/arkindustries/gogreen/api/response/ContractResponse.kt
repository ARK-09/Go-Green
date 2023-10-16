package com.arkindustries.gogreen.api.response

data class ContractResponse(
    val _id: String,
    val proposalId: String,
    val startTime: String,
    val endTime: String,
    val amount: Double,
    val status: String
)