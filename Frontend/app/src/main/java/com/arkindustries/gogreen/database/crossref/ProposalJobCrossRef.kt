package com.arkindustries.gogreen.database.crossref

import androidx.room.Entity

@Entity(tableName= "proposal_job_cross_ref" ,primaryKeys = ["proposalId", "jobId"])
data class ProposalJobCrossRef(
    val proposalId: String,
    val jobId: String
)