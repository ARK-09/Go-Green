package com.arkindustries.gogreen.database.crossref

import androidx.room.Entity

@Entity(primaryKeys = ["jobId", "attachmentId"])
data class JobAttachmentCrossRef(
    val jobId: String,
    val attachmentId: String
)