package com.arkindustries.gogreen.api.response

data class RoomMessage(
    val _id: String,
    val text: String,
    val attachments: List<AttachmentResponse>,
    val sender: User,
    val status: String,
    val createdDate: String,
    val room: Room
)

data class RoomMessageUnpopulated(
    val _id: String,
    val text: String,
    val attachments: List<AttachmentResponse>,
    val sender: User,
    val status: String,
    val createdDate: String,
    val room: String
)