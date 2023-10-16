package com.arkindustries.gogreen.database.entites

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.arkindustries.gogreen.database.crossref.ProposalJobCrossRef

data class ProposalWithJob(
    @Embedded
    val proposal: ProposalEntity,

    @Relation(
        parentColumn = "proposalId",
        entityColumn = "jobId",
        associateBy = Junction(ProposalJobCrossRef::class)
    )
    var job: JobEntity
)