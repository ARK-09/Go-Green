package com.arkindustries.gogreen.database.entites

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.arkindustries.gogreen.database.crossref.ProposalAttachmentCrossRef
import com.arkindustries.gogreen.database.crossref.ProposalJobCrossRef

data class ProposalWithAttachmentsAndJob(
    @Embedded
    val proposal: ProposalEntity,

    @Relation(
        parentColumn = "proposalId",
        entityColumn = "attachmentId",
        associateBy = Junction(ProposalAttachmentCrossRef::class)
    )
    var attachments: List<AttachmentEntity>,

    @Relation(
        parentColumn = "proposalId",
        entityColumn = "jobId",
        associateBy = Junction(ProposalJobCrossRef::class)
    )
    var job: JobEntity
)