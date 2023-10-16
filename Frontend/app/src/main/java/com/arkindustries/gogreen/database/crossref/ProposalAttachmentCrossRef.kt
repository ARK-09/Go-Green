package com.arkindustries.gogreen.database.crossref

import androidx.room.Entity

@Entity(tableName= "proposal_attachment_cross_ref" ,primaryKeys = ["proposalId", "attachmentId"])
data class ProposalAttachmentCrossRef(
    val proposalId: String,
    val attachmentId: String
)