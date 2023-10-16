package com.arkindustries.gogreen.database.entites

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.arkindustries.gogreen.database.crossref.JobAttachmentCrossRef

data class JobWithAttachments(
    @Embedded
    val job: JobEntity,

    @Relation(
        parentColumn = "jobId",
        entityColumn = "attachmentId",
        associateBy = Junction(JobAttachmentCrossRef::class)
    )
    val attachments: List<AttachmentEntity>
)