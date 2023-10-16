package com.arkindustries.gogreen.database.crossref

import androidx.room.Entity

@Entity(tableName = "job_attachment_cross_ref", primaryKeys = ["jobId", "attachmentId"])
data class JobAttachmentCrossRef(
    val jobId: String,
    val attachmentId: String
)