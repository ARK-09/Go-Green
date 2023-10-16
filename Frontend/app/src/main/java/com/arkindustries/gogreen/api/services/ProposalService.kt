package com.arkindustries.gogreen.api.services

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
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ProposalService {
    @POST("proposals/jobs/{id}")
    suspend fun createJobProposal(
        @Path("id") jobId: String,
        @Body request: CreateJobProposalRequest
    ): ApiResponse<ProposalResponse>

    @POST("proposals/{id}/interviews")
    suspend fun createInterview(
        @Path("id") proposalId: String,
    ): ApiResponse<CreateInterviewResponse>

    @POST("proposals/{id}/jobs/{jobid}")
    suspend fun hireProposal(
        @Path("id") proposalId: String,
        @Path("jobid") jobId: String,
        ): ApiResponse<HireProposalResponse>

    @GET("proposals")
    suspend fun getProposalsByUser(): ApiResponse<ProposalsResponse>

    @GET("proposals/users/{userid}/reviews")
    suspend fun getUserReviews(@Path("userid") userId: String): ApiResponse<ReviewsResponse>

    @POST("proposals/:id")
    suspend fun createProposalFeedback(
        @Path("id") proposalId: String,
        @Body request: ReviewRequest
    ): ApiResponse<ProposalResponse>

    @GET("proposals/jobs/{id}")
    suspend fun getJobProposals(@Path("id") jobId: String): ApiResponse<ProposalsResponse>

    @POST("proposals/services/{id}")
    suspend fun createServiceProposal(
        @Path("id") serviceId: String,
        @Body request: CreateJobProposalRequest
    ): ApiResponse<ProposalResponse>

    @GET("proposals/services/{id}")
    suspend fun getServiceProposals(@Path("id") serviceId: String): ApiResponse<ProposalsResponse>

    @GET("proposals/{id}")
    suspend fun getProposalById(@Path("id") proposalId: String): ApiResponse<ProposalResponse>

    @DELETE("proposals/{id}")
    suspend fun deleteProposal(@Path("id") proposalId: String): ApiResponse<Unit>

    @GET("proposals/jobs/{id}/attachments/{attachmentid}")
    suspend fun getProposalAttachment(
        @Path("id") jobId: String,
        @Path("attachmentid") attachmentId: String
    ): ApiResponse<AttachmentResponse>

    @POST("proposals/{id}/attachments")
    suspend fun addProposalAttachment(
        @Path("id") proposalId: String,
        @Body request: List<AttachmentRequest>
    ): ApiResponse<AttachmentResponse>

    @GET("proposals/{id}/attachments")
    suspend fun getProposalAttachments(@Path("id") proposalId: String): ApiResponse<List<AttachmentResponse>>

    @GET("proposals/{id}/attachments/{attachmentid}")
    suspend fun getProposalAttachmentById(
        @Path("id") proposalId: String,
        @Path("attachmentid") attachmentId: String
    ): ApiResponse<AttachmentResponse>

    @DELETE("proposals/{id}/attachments/{attachmentid}")
    suspend fun deleteProposalAttachment(
        @Path("id") proposalId: String,
        @Path("attachmentid") attachmentId: String
    ): ApiResponse<Unit>
}
