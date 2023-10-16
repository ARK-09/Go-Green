package com.arkindustries.gogreen.database.entites

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("attachments")
class AttachmentEntity(
    @PrimaryKey
    val attachmentId: String,
    val mimeType: String,
    val originalName: String,
    val createdDate: String?,
    val url: String? = null
)