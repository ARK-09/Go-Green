package com.arkindustries.gogreen.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.arkindustries.gogreen.database.crossref.JobAttachmentCrossRef
import com.arkindustries.gogreen.database.crossref.JobCategoryCrossRef
import com.arkindustries.gogreen.database.crossref.JobSkillCrossRef
import com.arkindustries.gogreen.database.entites.JobEntity
import com.arkindustries.gogreen.database.entites.JobWithCategoriesAndSkillsAndAttachments

@Dao
interface JobDao {

    @Query("SELECT * FROM jobs")
    suspend fun getJobs (): List<JobEntity>

    @Upsert
    suspend fun upsertJobs(job: List<JobEntity>)

    @Upsert
    suspend fun upsertJobWithSkills (jobWithSkills: List<JobSkillCrossRef>)

    @Upsert
    suspend fun upsertJobWithAttachments (jobWithAttachments: List<JobAttachmentCrossRef>)

    @Upsert
    suspend fun upsertJobWithCategories (jobWithCategories: List<JobCategoryCrossRef>)

    @Query ("DELETE FROM jobs WHERE jobId= :jobId")
    suspend fun deleteJob(jobId: String)

    @Query ("DELETE FROM jobs")
    suspend fun deleteJobs ()

    @Query ("SELECT * FROM jobs WHERE userId= :userId")
    suspend fun getJobsByUser (userId: String): List<JobEntity>

    @Transaction
    @Query("SELECT * FROM jobs")
    suspend fun getJobsWithCategoriesAndSkillsAndAttachments(): List<JobWithCategoriesAndSkillsAndAttachments>

    @Query("SELECT * FROM jobs WHERE jobId = :jobId")
    suspend fun getJobById(jobId: String): JobEntity?

    @Transaction
    @Query("SELECT * FROM jobs WHERE jobId = :jobId")
    suspend fun getJobWithCategoriesAndSkillsAndAttachments(jobId: String): JobWithCategoriesAndSkillsAndAttachments
}