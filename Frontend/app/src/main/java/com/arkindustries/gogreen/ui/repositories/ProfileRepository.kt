package com.arkindustries.gogreen.ui.repositories

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
import com.arkindustries.gogreen.api.services.ProfileService
import com.arkindustries.gogreen.utils.handleApiCall

class ProfileRepository(private val profileService: ProfileService) {

    suspend fun getUserProfile(id: String): ApiResponse<UserProfileResponse> {
        return handleApiCall {
            profileService.getUserProfile(id)
        }
    }

    suspend fun updateUserProfile(id: String, request: UpdateUserProfileRequest): ApiResponse<UserProfileResponse> {
        return handleApiCall {
            profileService.updateUserProfile(id, request)
        }
    }

    suspend fun deleteUserProfile(id: String): ApiResponse<Unit> {
        return handleApiCall {
            profileService.deleteUserProfile(id)
        }
    }

    suspend fun createProject(request: CreateProjectRequest): ApiResponse<ProjectResponse> {
        return handleApiCall {
            profileService.createProject(request)
        }
    }

    suspend fun getUserProjects(profileId: String): ApiResponse<ProjectsResponse> {
        return handleApiCall {
            profileService.getUserProjects(profileId)
        }
    }

    suspend fun getProjectDetails(id: String, projectId: String): ApiResponse<ProjectResponse> {
        return handleApiCall {
            profileService.getProjectDetails(id, projectId)
        }
    }

    suspend fun deleteProject(id: String, projectId: String): ApiResponse<Unit> {
        return handleApiCall {
            profileService.deleteProject(id, projectId)
        }
    }

    suspend fun updateProject(id: String, projectId: String, request: UpdateProjectRequest): ApiResponse<ProjectResponse> {
        return handleApiCall {
            profileService.updateProject(id, projectId, request)
        }
    }

    suspend fun addProjectAttachment(
        id: String,
        projectId: String,
        request: AttachmentRequest
    ): ApiResponse<AttachmentResponse> {
        return handleApiCall {
            profileService.addProjectAttachment(id, projectId, request)
        }
    }

    suspend fun getProjectAttachments(
        id: String,
        projectId: String
    ): ApiResponse<List<AttachmentResponse>> {
        return handleApiCall {
            profileService.getProjectAttachments(id, projectId)
        }
    }

    suspend fun getAttachmentDetails(
        id: String,
        projectId: String,
        attachmentId: String
    ): ApiResponse<AttachmentResponse> {
        return handleApiCall {
            profileService.getAttachmentDetails(id, projectId, attachmentId)
        }
    }

    suspend fun deleteAttachment(
        id: String,
        projectId: String,
        attachmentId: String
    ): ApiResponse<Unit> {
        return handleApiCall {
            profileService.deleteAttachment(id, projectId, attachmentId)
        }
    }

    suspend fun deleteAllAttachments(
        id: String,
        projectId: String
    ): ApiResponse<Unit> {
        return handleApiCall {
            profileService.deleteAllAttachments(id, projectId)
        }
    }

    suspend fun addSkillsToProject(
        id: String,
        projectId: String,
        request: SkillsRequest
    ): ApiResponse<Unit> {
        return handleApiCall {
            profileService.addSkillsToProject(id, projectId, request)
        }
    }

    suspend fun getProjectSkills(
        id: String,
        projectId: String
    ): ApiResponse<SkillsResponse> {
        return handleApiCall {
            profileService.getProjectSkills(id, projectId)
        }
    }

    suspend fun deleteSkillFromProject(
        id: String,
        projectId: String,
        skillId: String
    ): ApiResponse<Unit> {
        return handleApiCall {
            profileService.deleteSkillFromProject(id, projectId, skillId)
        }
    }

    suspend fun deleteAllSkillsFromProject(
        id: String,
        projectId: String
    ): ApiResponse<Unit> {
        return handleApiCall {
            profileService.deleteAllSkillsFromProject(id, projectId)
        }
    }
}
