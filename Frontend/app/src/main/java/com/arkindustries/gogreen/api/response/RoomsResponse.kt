package com.arkindustries.gogreen.api.response

data class RoomResponse(
    val room: Room
)

data class Room(
    val id: String,
    val name: String,
    val members: List<RoomMemberResponse>
)