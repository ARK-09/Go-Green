package com.arkindustries.gogreen.database.crossref

import androidx.room.Entity

@Entity(tableName = "user_proposal_cross_ref", primaryKeys = ["userId", "proposalId"])
data class UserProposalCrossRef(
    val userId: String,
    val proposalId: String
)