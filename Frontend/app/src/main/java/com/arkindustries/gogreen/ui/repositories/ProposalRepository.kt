package com.arkindustries.gogreen.ui.repositories

import com.arkindustries.gogreen.api.request.AttachmentRequest
import com.arkindustries.gogreen.api.request.CreateJobProposalRequest
import com.arkindustries.gogreen.api.request.ReviewRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.AttachmentResponse
import com.arkindustries.gogreen.api.response.CreateInterviewResponse
import com.arkindustries.gogreen.api.response.HireProposalResponse
import com.arkindustries.gogreen.api.response.ProposalResponse
import com.arkindustries.gogreen.api.response.ProposalsResponse
import com.arkindustries.gogreen.api.response.ReviewsResponse
import com.arkindustries.gogreen.api.services.ProposalService
import com.arkindustries.gogreen.database.crossref.ProposalJobCrossRef
import com.arkindustries.gogreen.database.dao.ProposalDao
import com.arkindustries.gogreen.database.entites.ProposalEntity
import com.arkindustries.gogreen.database.entites.ProposalWithAttachments
import com.arkindustries.gogreen.database.entites.ProposalWithAttachmentsAndJob
import com.arkindustries.gogreen.database.entites.ProposalWithJob
import com.arkindustries.gogreen.utils.handleApiCall

class ProposalRepository(
    private val proposalDao: ProposalDao,
    private val proposalService: ProposalService
) {
    suspend fun createJobProposalAtServer(
        jobId: String,
        request: CreateJobProposalRequest
    ): ApiResponse<ProposalResponse> {
        return handleApiCall {
            proposalService.createJobProposal(jobId, request)
        }
    }

    suspend fun createInterview(
        proposalId: String
    ): ApiResponse<CreateInterviewResponse> {
        return handleApiCall {
            proposalService.createInterview(proposalId)
        }
    }

    suspend fun hireProposal(
        proposalId: String,
        jobId: String
    ): ApiResponse<HireProposalResponse> {
        return handleApiCall {
            proposalService.hireProposal(proposalId, jobId)
        }
    }

    suspend fun getUserReviews(userId: String): ApiResponse<ReviewsResponse> {
        return handleApiCall {
            proposalService.getUserReviews(userId)
        }
    }

    suspend fun createProposalFeedback(
        proposalId: String,
        request: ReviewRequest
    ): ApiResponse<ProposalResponse> {
        return handleApiCall {
            proposalService.createProposalFeedback(proposalId, request)
        }
    }

    suspend fun getJobProposalsFromServer(jobId: String): ApiResponse<ProposalsResponse> {
        return handleApiCall {
            proposalService.getJobProposals(jobId)
        }
    }

    suspend fun getProposalsByUserFromServer(): ApiResponse<ProposalsResponse> {
        return handleApiCall {
            proposalService.getProposalsByUser()
        }
    }

    suspend fun createServiceProposalAtServer(
        serviceId: String,
        request: CreateJobProposalRequest
    ): ApiResponse<ProposalResponse> {
        return handleApiCall {
            proposalService.createServiceProposal(serviceId, request)
        }
    }

    suspend fun getServiceProposalsFromServer(serviceId: String): ApiResponse<ProposalsResponse> {
        return handleApiCall {
            proposalService.getServiceProposals(serviceId)
        }
    }

    suspend fun getProposalByIdFromServer(proposalId: String): ApiResponse<ProposalResponse> {
        return handleApiCall {
            proposalService.getProposalById(proposalId)
        }
    }

    suspend fun deleteProposalFromServer(proposalId: String): ApiResponse<Unit> {
        return handleApiCall {
            proposalService.deleteProposal(proposalId)
        }
    }

    suspend fun deleteProposalAttachmentCrossRef(proposalId: String, attachmentId: String) {
        return proposalDao.deleteProposalAttachmentCrossRef(proposalId, attachmentId)
    }

    suspend fun deleteJobProposalAttachmentFromServer(
        proposalId: String,
        attachmentId: String
    ): ApiResponse<Unit> {
        return handleApiCall {
            proposalService.deleteProposalAttachment(proposalId, attachmentId)
        }
    }

    suspend fun addProposalAttachmentAtServer(
        proposalId: String,
        request: List<AttachmentRequest>
    ): ApiResponse<AttachmentResponse> {
        return handleApiCall {
            proposalService.addProposalAttachment(proposalId, request)
        }
    }

    suspend fun getProposalAttachmentsFromServer(proposalId: String): ApiResponse<List<AttachmentResponse>> {
        return handleApiCall {
            proposalService.getProposalAttachments(proposalId)
        }
    }

    suspend fun getProposalAttachmentByIdFromServer(
        proposalId: String,
        attachmentId: String
    ): ApiResponse<AttachmentResponse> {
        return handleApiCall {
            proposalService.getProposalAttachmentById(proposalId, attachmentId)
        }
    }

    suspend fun deleteProposalAttachmentFromServer(
        proposalId: String,
        attachmentId: String
    ): ApiResponse<Unit> {
        return handleApiCall {
            proposalService.deleteProposalAttachment(proposalId, attachmentId)
        }
    }

    suspend fun upsertProposalsAtLocal(proposal: List<ProposalEntity>) {
        return proposalDao.upsertProposals(proposal)
    }

    suspend fun upsertProposalWithJobAtLocal(proposalWithJob: List<ProposalJobCrossRef>) {
        proposalDao.upsertProposalWithJob(proposalWithJob)
    }

    suspend fun deleteAllFromLocal() {
        return proposalDao.deleteAll()
    }

    suspend fun deleteAllProposalJobCrossRefFromLocal() {
        return proposalDao.deleteProposalJobCrossRef()
    }

    suspend fun deleteAllProposalUserCrossRefFromLocal() {
        return proposalDao.deleteProposalUserCrossRef()
    }

    suspend fun deleteByIdFromLocal(proposalId: String) {
        return proposalDao.deleteById(proposalId)
    }

    suspend fun getJobProposalsWithAttachmentsFromLocal(jobId: String): List<ProposalWithAttachments> {
        return proposalDao.getJobProposalsWithAttachments(jobId)
    }

    suspend fun getJobProposalsWithAttachmentsAndJobFromLocal(jobId: String): List<ProposalWithAttachmentsAndJob> {
        return proposalDao.getJobProposalsWithAttachmentsAndJob(jobId)
    }

    suspend fun getProposalWithJobFromLocal(proposalId: String): List<ProposalWithJob> {
        return proposalDao.getProposalWithJob(proposalId)
    }


    suspend fun getProposalsByUserFromLocal(userId: String): List<ProposalWithAttachmentsAndJob> {
        return proposalDao.getProposalsByUser(userId)
    }

    suspend fun getProposalByIdFromLocal(proposalId: String): ProposalWithAttachmentsAndJob? {
        return proposalDao.getProposalById(proposalId)
    }
}