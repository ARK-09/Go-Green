package com.arkindustries.gogreen.database.entites

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.arkindustries.gogreen.database.crossref.JobSkillCrossRef

data class JobWithSkills(
    @Embedded
    val job: JobEntity,

    @Relation(
        parentColumn = "jobId",
        entityColumn = "attachmentId",
        associateBy = Junction(JobSkillCrossRef::class)
    )
    val skills: List<SkillEntity>
)