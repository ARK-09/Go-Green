package com.arkindustries.gogreen.api.services

import com.arkindustries.gogreen.api.request.AttachmentRequest
import com.arkindustries.gogreen.api.request.CreateJobRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.AttachmentResponse
import com.arkindustries.gogreen.api.response.JobResponse
import com.arkindustries.gogreen.api.response.JobSearchResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface JobService {
    @POST("jobs")
    suspend fun createJob(@Body request: CreateJobRequest): ApiResponse<JobResponse>

    @GET("jobs")
    suspend fun getJobs(): ApiResponse<JobSearchResponse>

    @GET("jobs/search")
    suspend fun searchJobs(
        @Query("q") query: String?,
        @Query("offset") offset: Int?,
        @Query("limit") limit: Int?,
        @Query("location[coordinates][0]") latitude: Double?,
        @Query("location[coordinates][1]") longitude: Double?,
        @Query("price") price: String?
    ): ApiResponse<JobSearchResponse>

    @GET("jobs/{id}")
    suspend fun getJobById(@Path("id") jobId: String): ApiResponse<JobResponse>

    @PATCH("jobs/{id}")
    suspend fun updateJob(@Path("id") jobId: String, @Body request: CreateJobRequest): ApiResponse<JobResponse>

    @DELETE("jobs/{id}")
    suspend fun deleteJob(@Path("id") jobId: String): ApiResponse<Unit>

    @POST("jobs/{id}/attachments")
    suspend fun addJobAttachment(
        @Path("id") jobId: String,
        @Body request: AttachmentRequest
    ): ApiResponse<AttachmentResponse>

    @GET("jobs/{id}/attachments")
    suspend fun getJobAttachments(@Path("id") jobId: String): ApiResponse<List<AttachmentResponse>>

    @GET("jobs/{id}/attachments/{attachmentid}")
    suspend fun getJobAttachmentById(
        @Path("id") jobId: String,
        @Path("attachmentid") attachmentId: String
    ): ApiResponse<AttachmentResponse>

    @DELETE("jobs/{id}/attachments/{attachmentid}")
    suspend fun deleteJobAttachment(
        @Path("id") jobId: String,
        @Path("attachmentid") attachmentId: String
    ): ApiResponse<Unit>
}
