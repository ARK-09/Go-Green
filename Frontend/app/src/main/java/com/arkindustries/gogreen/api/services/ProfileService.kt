package com.arkindustries.gogreen.api.services

import com.arkindustries.gogreen.api.request.AttachmentRequest
import com.arkindustries.gogreen.api.request.CreateProjectRequest
import com.arkindustries.gogreen.api.request.SkillsRequest
import com.arkindustries.gogreen.api.request.UpdateProjectRequest
import com.arkindustries.gogreen.api.request.UpdateUserProfileRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.AttachmentResponse
import com.arkindustries.gogreen.api.response.ProjectResponse
import com.arkindustries.gogreen.api.response.ProjectsResponse
import com.arkindustries.gogreen.api.response.SkillsResponse
import com.arkindustries.gogreen.api.response.UserProfileResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ProfileService {
    @GET("profiles/users/{id}")
    suspend fun getUserProfile(@Path("id") id: String): ApiResponse<UserProfileResponse>

    @PATCH("profiles/users/{id}")
    suspend fun updateUserProfile(
        @Path("id") id: String,
        @Body request: UpdateUserProfileRequest
    ): ApiResponse<UserProfileResponse>

    @DELETE("profiles/users/{id}")
    suspend fun deleteUserProfile(@Path("id") id: String): ApiResponse<Unit>

    @POST("profiles/projects")
    suspend fun createProject(
        @Body request: CreateProjectRequest
    ): ApiResponse<ProjectResponse>

    @GET("profiles/{id}/projects")
    suspend fun getUserProjects(@Path("id") profileId: String): ApiResponse<ProjectsResponse>

    @GET("profiles/{id}/projects/{projectid}")
    suspend fun getProjectDetails(
        @Path("id") id: String,
        @Path("projectid") projectId: String
    ): ApiResponse<ProjectResponse>

    @DELETE("profiles/{id}/projects/{projectid}")
    suspend fun deleteProject(
        @Path("id") id: String,
        @Path("projectid") projectId: String
    ): ApiResponse<Unit>

    @PATCH("profiles/{id}/projects/{projectid}")
    suspend fun updateProject(
        @Path("id") id: String,
        @Path("projectid") projectId: String,
        @Body request: UpdateProjectRequest
    ): ApiResponse<ProjectResponse>

    @POST("profiles/{id}/projects/{projectid}/attachments")
    suspend fun addProjectAttachment(
        @Path("id") id: String,
        @Path("projectid") projectId: String,
        @Body request: AttachmentRequest
    ): ApiResponse<AttachmentResponse>

    @GET("profiles/{id}/projects/{projectid}/attachments")
    suspend fun getProjectAttachments(
        @Path("id") id: String,
        @Path("projectid") projectId: String
    ): ApiResponse<List<AttachmentResponse>>

    @GET("profiles/{id}/projects/{projectid}/attachments/{attachmentid}")
    suspend fun getAttachmentDetails(
        @Path("id") id: String,
        @Path("projectid") projectId: String,
        @Path("attachmentid") attachmentId: String
    ): ApiResponse<AttachmentResponse>

    @DELETE("profiles/{id}/projects/{projectid}/attachments/{attachmentid}")
    suspend fun deleteAttachment(
        @Path("id") id: String,
        @Path("projectid") projectId: String,
        @Path("attachmentid") attachmentId: String
    ): ApiResponse<Unit>

    @DELETE("profiles/{id}/projects/{projectid}/attachments")
    suspend fun deleteAllAttachments(
        @Path("id") id: String,
        @Path("projectid") projectId: String
    ): ApiResponse<Unit>

    @POST("profiles/{id}/projects/{projectid}/skills")
    suspend fun addSkillsToProject(
        @Path("id") id: String,
        @Path("projectid") projectId: String,
        @Body request: SkillsRequest
    ): ApiResponse<Unit>

    @GET("profiles/{id}/projects/{projectid}/skills")
    suspend fun getProjectSkills(
        @Path("id") id: String,
        @Path("projectid") projectId: String
    ): ApiResponse<SkillsResponse>

    @DELETE("profiles/{id}/projects/{projectid}/skills/{skillld}")
    suspend fun deleteSkillFromProject(
        @Path("id") id: String,
        @Path("projectid") projectId: String,
        @Path("skillld") skillId: String
    ): ApiResponse<Unit>

    @DELETE("profiles/{id}/projects/{projectid}/skills")
    suspend fun deleteAllSkillsFromProject(
        @Path("id") id: String,
        @Path("projectid") projectId: String
    ): ApiResponse<Unit>
}