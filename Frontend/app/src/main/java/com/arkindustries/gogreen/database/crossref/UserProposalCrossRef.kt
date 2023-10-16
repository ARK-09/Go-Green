package com.arkindustries.gogreen.database.crossref

import androidx.room.Entity

@Entity(primaryKeys = ["jobId", "proposalId"])
data class ProposalUserCrossRef(
    val jobId: String,
    val proposalId: String
)