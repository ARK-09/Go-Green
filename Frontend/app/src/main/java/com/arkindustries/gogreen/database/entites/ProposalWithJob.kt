package com.arkindustries.gogreen.database.entites

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.arkindustries.gogreen.database.crossref.ProposalAttachmentCrossRef

data class ProposalWithAttachments(
    @Embedded
    val proposal: ProposalEntity,

    @Relation(
        parentColumn = "proposalId",
        entityColumn = "attachmentId",
        associateBy = Junction(ProposalAttachmentCrossRef::class)
    )
    var attachments: List<AttachmentEntity>
)