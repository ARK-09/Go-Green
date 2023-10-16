package com.arkindustries.gogreen.database.entites

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.arkindustries.gogreen.database.crossref.JobAttachmentCrossRef
import com.arkindustries.gogreen.database.crossref.JobCategoryCrossRef
import com.arkindustries.gogreen.database.crossref.JobSkillCrossRef


data class JobWithCategoriesAndSkillsAndAttachments(
    @Embedded
    val job: JobEntity,

    @Relation(
        parentColumn = "jobId",
        entityColumn = "attachmentId",
        associateBy = Junction(JobAttachmentCrossRef::class)
    )
    val attachments: List<AttachmentEntity>,

    @Relation(
        parentColumn = "jobId",
        entityColumn = "skillId",
        associateBy = Junction(JobSkillCrossRef::class)
    )
    val skills: List<SkillEntity>,

    @Relation(
        parentColumn = "jobId",
        entityColumn = "categoryId",
        associateBy = Junction(JobCategoryCrossRef::class)
    )
    val categories: List<CategoryEntity>
)