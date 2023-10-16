package com.arkindustries.gogreen.database.entites

import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.arkindustries.gogreen.database.crossref.JobAttachmentCrossRef
import com.arkindustries.gogreen.database.crossref.JobCategoryCrossRef
import com.arkindustries.gogreen.database.crossref.JobSkillCrossRef
import java.util.LinkedList

@DatabaseView(
    viewName = "job_with_relations",
    value = """
        SELECT
            jobs.*,
            attachments.*,
            skills.*,
            categories.*
        FROM jobs
        LEFT JOIN job_attachment_cross_ref ON jobs.jobId = job_attachment_cross_ref.jobId
        LEFT JOIN attachments ON job_attachment_cross_ref.attachmentId = attachments.attachmentId
        LEFT JOIN job_skill_cross_ref ON jobs.jobId = job_skill_cross_ref.jobId
        LEFT JOIN skills ON job_skill_cross_ref.skillId = skills.skillId
        LEFT JOIN job_category_cross_ref ON jobs.jobId = job_category_cross_ref.jobId
        LEFT JOIN categories ON job_category_cross_ref.categoryId = categories.categoryId
    """
)
data class JobWithCategoriesAndSkillsAndAttachments(
    @Embedded
    val job: JobEntity,

    @Relation(
        parentColumn = "jobId",
        entityColumn = "attachmentId",
        associateBy = Junction(JobAttachmentCrossRef::class)
    )
    val attachments: LinkedList<AttachmentEntity>,

    @Relation(
        parentColumn = "jobId",
        entityColumn = "skillId",
        associateBy = Junction(JobSkillCrossRef::class)
    )
    val skills: LinkedList<SkillEntity>,

    @Relation(
        parentColumn = "jobId",
        entityColumn = "categoryId",
        associateBy = Junction(JobCategoryCrossRef::class)
    )
    val categories: LinkedList<CategoryEntity>
)