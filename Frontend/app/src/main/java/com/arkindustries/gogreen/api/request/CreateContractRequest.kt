package com.arkindustries.gogreen.api.request

data class CreateContractRequest(
    val proposalId: String,
    val amount: Double
)