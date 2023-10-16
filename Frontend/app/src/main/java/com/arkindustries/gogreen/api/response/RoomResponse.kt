package com.arkindustries.gogreen.api.response

data class RoomResponse(
    val room: Room
)

data class Room(
    val _id: String,
    val name: String,
    val lastMessage: RoomMessageUnpopulated?,
    val members: List<User>,
    val owner: User,
    val createdDate: String,
    val unreadMessages: Int
)