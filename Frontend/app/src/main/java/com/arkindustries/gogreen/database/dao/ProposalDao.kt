package com.arkindustries.gogreen.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.arkindustries.gogreen.database.entites.JobEntity
import com.arkindustries.gogreen.database.entites.JobWithCategoriesAndSkillsAndAttachments

@Dao
interface JobDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobEntity>)

    @Transaction
    @Query("SELECT * FROM jobs")
    suspend fun getAllJobs(): List<JobWithCategoriesAndSkillsAndAttachments>

    @Transaction
    @Query("SELECT * FROM jobs WHERE jobId = :jobId")
    suspend fun getJobById(jobId: String): JobWithCategoriesAndSkillsAndAttachments?

    @Query("DELETE FROM jobs WHERE jobId = :jobId")
    suspend fun deleteJobById(jobId: String)

    // Additional methods for relationships with categories, skills, attachments, and user
    @Transaction
    @Query("SELECT * FROM jobs")
    suspend fun getJobsWithCategoriesSkillsAttachmentsAndUser(): List<JobWithCategoriesAndSkillsAndAttachments>

    @Transaction
    @Query("SELECT * FROM jobs WHERE jobId = :jobId")
    suspend fun getJobWithCategoriesSkillsAttachmentsAndUserById(jobId: String): JobWithCategoriesAndSkillsAndAttachments?
}
