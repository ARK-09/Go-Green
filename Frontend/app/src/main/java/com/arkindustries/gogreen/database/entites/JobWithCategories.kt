package com.arkindustries.gogreen.database.entites

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.arkindustries.gogreen.database.crossref.JobCategoryCrossRef
import com.arkindustries.gogreen.database.crossref.JobSkillCrossRef

data class JobWithCategories(
    @Embedded
    val job: JobEntity,

    @Relation(
        parentColumn = "jobId",
        entityColumn = "categoryId",
        associateBy = Junction(JobCategoryCrossRef::class)
    )
    val categories: List<CategoryEntity>
)