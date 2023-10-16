package com.arkindustries.gogreen.api.request

data class CreateRoomRequest(
    val name: String,
    val members: List<String>
)
