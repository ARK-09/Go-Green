package com.arkindustries.gogreen.ui.repositories

import com.arkindustries.gogreen.api.request.AttachmentRequest
import com.arkindustries.gogreen.api.request.CreateJobRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.AttachmentResponse
import com.arkindustries.gogreen.api.response.JobResponse
import com.arkindustries.gogreen.api.response.JobSearchResponse
import com.arkindustries.gogreen.api.services.JobService
import com.arkindustries.gogreen.database.dao.JobDao
import com.arkindustries.gogreen.database.entites.JobEntity
import com.arkindustries.gogreen.database.entites.JobWithCategoriesAndSkillsAndAttachments
import com.arkindustries.gogreen.utils.handleApiCall

class JobRepository(private val jobDao: JobDao, private val jobService: JobService) {
    suspend fun createJob(request: CreateJobRequest): ApiResponse<JobResponse> {
        return handleApiCall {
         jobService.createJob(request)
        }
    }

    suspend fun getJobsByCurrentUserFromServer(): ApiResponse<List<JobResponse>> {
        return handleApiCall {
         jobService.getJobs()
        }
    }

    suspend fun searchJobs(
        query: String?,
        offset: Int?,
        limit: Int?,
        latitude: Double?,
        longitude: Double?,
        price: String?
    ): ApiResponse<JobSearchResponse> {
        return handleApiCall {
         jobService.searchJobs(query, offset, limit, latitude, longitude, price)
        }
    }

    suspend fun getJobByIdFromServer(jobId: String): ApiResponse<JobResponse> {
        return handleApiCall {
         jobService.getJobById(jobId)
        }
    }

    suspend fun updateJobAtServer(jobId: String, job: CreateJobRequest): ApiResponse<JobResponse> {
        return handleApiCall {
         jobService.updateJob(jobId, job)
        }
    }

    suspend fun deleteJobAtServer(jobId: String): ApiResponse<Unit> {
        return handleApiCall {
         jobService.deleteJob(jobId)
        }
    }

    suspend fun addJobAttachmentAtServer(
        jobId: String,
        attachment: AttachmentRequest
    ): ApiResponse<AttachmentResponse> {
        return handleApiCall {
         jobService.addJobAttachment(jobId, attachment)
        }
    }

    suspend fun getJobAttachmentsFromServer(jobId: String): ApiResponse<List<AttachmentResponse>> {
        return handleApiCall {
         jobService.getJobAttachments(jobId)
        }
    }

    suspend fun getJobAttachmentByIdFromServer(
        jobId: String,
        attachmentId: String
    ): ApiResponse<AttachmentResponse> {
        return handleApiCall {
         jobService.getJobAttachmentById(jobId, attachmentId)
        }
    }

    suspend fun deleteJobAttachmentAtServer(
        jobId: String,
        attachmentId: String
    ): ApiResponse<Unit> {
        return handleApiCall {
         jobService.deleteJobAttachment(jobId, attachmentId)
        }
    }

    suspend fun getJobsFromLocalServer (): List<JobEntity> {
        return jobDao.getJobs()
    }

    suspend fun getJobsByCurrentUser (userId: String): List<JobEntity> {
        return jobDao.getJobsByUser (userId)
    }

    suspend fun upsertJobs(job: List<JobEntity>) {
        jobDao.upsertJobs(job)
    }

    suspend fun deleteJobFromLocal(jobId: String) {
        jobDao.deleteJob(jobId)
    }

    suspend fun getJobsWithCategoriesAndSkillsAndAttachmentsFromLocal(): List<JobWithCategoriesAndSkillsAndAttachments> {
        return jobDao.getJobsWithCategoriesAndSkillsAndAttachments()
    }

    suspend fun getJobByIdFromLocal(jobId: String): JobEntity? {
        return jobDao.getJobById(jobId)
    }

    suspend fun getJobWithCategoriesAndSkillsAndAttachmentsFromLocal(jobId: String): List<JobWithCategoriesAndSkillsAndAttachments> {
        return jobDao.getJobWithCategoriesAndSkillsAndAttachments(jobId)
    }
}