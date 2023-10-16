package com.arkindustries.gogreen.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.arkindustries.gogreen.ui.repositories.ProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val profileRepository: ProfileRepository) : ViewModel() {
    private val _loadingUserProfile = MutableLiveData<Boolean>()
    val loadingUserProfile: LiveData<Boolean> get() = _loadingUserProfile

    private val _loadingUpdateUserProfile = MutableLiveData<Boolean>()
    val loadingUpdateUserProfile: LiveData<Boolean> get() = _loadingUpdateUserProfile

    private val _loadingDeleteUserProfile = MutableLiveData<Boolean>()
    val loadingDeleteUserProfile: LiveData<Boolean> get() = _loadingDeleteUserProfile

    private val _loadingCreateProject = MutableLiveData<Boolean>()
    val loadingCreateProject: LiveData<Boolean> get() = _loadingCreateProject

    private val _loadingUserProjects = MutableLiveData<Boolean>()
    val loadingUserProjects: LiveData<Boolean> get() = _loadingUserProjects

    private val _loadingProjectDetails = MutableLiveData<Boolean>()
    val loadingProjectDetails: LiveData<Boolean> get() = _loadingProjectDetails

    private val _loadingDeleteProject = MutableLiveData<Boolean>()
    val loadingDeleteProject: LiveData<Boolean> get() = _loadingDeleteProject

    private val _loadingUpdateProject = MutableLiveData<Boolean>()
    val loadingUpdateProject: LiveData<Boolean> get() = _loadingUpdateProject

    private val _loadingAddProjectAttachment = MutableLiveData<Boolean>()
    val loadingAddProjectAttachment: LiveData<Boolean> get() = _loadingAddProjectAttachment

    private val _loadingGetProjectAttachments = MutableLiveData<Boolean>()
    val loadingGetProjectAttachments: LiveData<Boolean> get() = _loadingGetProjectAttachments

    private val _loadingGetAttachmentDetails = MutableLiveData<Boolean>()
    val loadingGetAttachmentDetails: LiveData<Boolean> get() = _loadingGetAttachmentDetails

    private val _loadingDeleteAttachment = MutableLiveData<Boolean>()
    val loadingDeleteAttachment: LiveData<Boolean> get() = _loadingDeleteAttachment

    private val _loadingDeleteAllAttachments = MutableLiveData<Boolean>()
    val loadingDeleteAllAttachments: LiveData<Boolean> get() = _loadingDeleteAllAttachments

    private val _loadingAddSkillsToProject = MutableLiveData<Boolean>()
    val loadingAddSkillsToProject: LiveData<Boolean> get() = _loadingAddSkillsToProject

    private val _loadingGetProjectSkills = MutableLiveData<Boolean>()
    val loadingGetProjectSkills: LiveData<Boolean> get() = _loadingGetProjectSkills

    private val _loadingDeleteSkillFromProject = MutableLiveData<Boolean>()
    val loadingDeleteSkillFromProject: LiveData<Boolean> get() = _loadingDeleteSkillFromProject

    private val _loadingDeleteAllSkillsFromProject = MutableLiveData<Boolean>()
    val loadingDeleteAllSkillsFromProject: LiveData<Boolean> get() = _loadingDeleteAllSkillsFromProject

    private val _userProfileResponse = MutableLiveData<UserProfileResponse>()
    val userProfileResponse: LiveData<UserProfileResponse> get() = _userProfileResponse

    private val _updateUserProfileResponse = MutableLiveData<UserProfileResponse>()
    val updateUserProfileResponse: LiveData<UserProfileResponse> get() = _updateUserProfileResponse

    private val _deleteUserProfileResponse = MutableLiveData<Unit>()
    val deleteUserProfileResponse: LiveData<Unit> get() = _deleteUserProfileResponse

    private val _createProjectResponse = MutableLiveData<ProjectResponse>()
    val createProjectResponse: LiveData<ProjectResponse> get() = _createProjectResponse

    private val _userProjectsResponse = MutableLiveData<ProjectsResponse>()
    val userProjectsResponse: LiveData<ProjectsResponse> get() = _userProjectsResponse

    private val _projectDetailsResponse = MutableLiveData<ProjectResponse>()
    val projectDetailsResponse: LiveData<ProjectResponse> get() = _projectDetailsResponse

    private val _deleteProjectResponse = MutableLiveData<Unit>()
    val deleteProjectResponse: LiveData<Unit> get() = _deleteProjectResponse

    private val _updateProjectResponse = MutableLiveData<ProjectResponse>()
    val updateProjectResponse: LiveData<ProjectResponse> get() = _updateProjectResponse

    private val _addProjectAttachmentResponse = MutableLiveData<AttachmentResponse>()
    val addProjectAttachmentResponse: LiveData<AttachmentResponse> get() = _addProjectAttachmentResponse

    private val _getProjectAttachmentsResponse = MutableLiveData<List<AttachmentResponse>>()
    val getProjectAttachmentsResponse: LiveData<List<AttachmentResponse>> get() = _getProjectAttachmentsResponse

    private val _getAttachmentDetailsResponse = MutableLiveData<AttachmentResponse>()
    val getAttachmentDetailsResponse: LiveData<AttachmentResponse> get() = _getAttachmentDetailsResponse

    private val _deleteAttachmentResponse = MutableLiveData<Unit>()
    val deleteAttachmentResponse: LiveData<Unit> get() = _deleteAttachmentResponse

    private val _deleteAllAttachmentsResponse = MutableLiveData<Unit>()
    val deleteAllAttachmentsResponse: LiveData<Unit> get() = _deleteAllAttachmentsResponse

    private val _addSkillsToProjectResponse = MutableLiveData<Unit>()
    val addSkillsToProjectResponse: LiveData<Unit> get() = _addSkillsToProjectResponse

    private val _getProjectSkillsResponse = MutableLiveData<SkillsResponse>()
    val getProjectSkillsResponse: LiveData<SkillsResponse> get() = _getProjectSkillsResponse

    private val _deleteSkillFromProjectResponse = MutableLiveData<Unit>()
    val deleteSkillFromProjectResponse: LiveData<Unit> get() = _deleteSkillFromProjectResponse

    private val _deleteAllSkillsFromProjectResponse = MutableLiveData<Unit>()
    val deleteAllSkillsFromProjectResponse: LiveData<Unit> get() = _deleteAllSkillsFromProjectResponse

    private val _errorUserProfile = MutableLiveData<ApiResponse<*>>()
    val errorUserProfile: LiveData<ApiResponse<*>> get() = _errorUserProfile

    private val _errorUpdateUserProfile = MutableLiveData<ApiResponse<*>>()
    val errorUpdateUserProfile: LiveData<ApiResponse<*>> get() = _errorUpdateUserProfile

    private val _errorDeleteUserProfile = MutableLiveData<ApiResponse<*>>()
    val errorDeleteUserProfile: LiveData<ApiResponse<*>> get() = _errorDeleteUserProfile

    private val _errorCreateProject = MutableLiveData<ApiResponse<*>>()
    val errorCreateProject: LiveData<ApiResponse<*>> get() = _errorCreateProject

    private val _errorUserProjects = MutableLiveData<ApiResponse<*>>()
    val errorUserProjects: LiveData<ApiResponse<*>> get() = _errorUserProjects

    private val _errorProjectDetails = MutableLiveData<ApiResponse<*>>()
    val errorProjectDetails: LiveData<ApiResponse<*>> get() = _errorProjectDetails

    private val _errorDeleteProject = MutableLiveData<ApiResponse<*>>()
    val errorDeleteProject: LiveData<ApiResponse<*>> get() = _errorDeleteProject

    private val _errorUpdateProject = MutableLiveData<ApiResponse<*>>()
    val errorUpdateProject: LiveData<ApiResponse<*>> get() = _errorUpdateProject

    private val _errorAddProjectAttachment = MutableLiveData<ApiResponse<*>>()
    val errorAddProjectAttachment: LiveData<ApiResponse<*>> get() = _errorAddProjectAttachment

    private val _errorGetProjectAttachments = MutableLiveData<ApiResponse<*>>()
    val errorGetProjectAttachments: LiveData<ApiResponse<*>> get() = _errorGetProjectAttachments

    private val _errorGetAttachmentDetails = MutableLiveData<ApiResponse<*>>()
    val errorGetAttachmentDetails: LiveData<ApiResponse<*>> get() = _errorGetAttachmentDetails

    private val _errorDeleteAttachment = MutableLiveData<ApiResponse<*>>()
    val errorDeleteAttachment: LiveData<ApiResponse<*>> get() = _errorDeleteAttachment

    private val _errorDeleteAllAttachments = MutableLiveData<ApiResponse<*>>()
    val errorDeleteAllAttachments: LiveData<ApiResponse<*>> get() = _errorDeleteAllAttachments

    private val _errorAddSkillsToProject = MutableLiveData<ApiResponse<*>>()
    val errorAddSkillsToProject: LiveData<ApiResponse<*>> get() = _errorAddSkillsToProject

    private val _errorGetProjectSkills = MutableLiveData<ApiResponse<*>>()
    val errorGetProjectSkills: LiveData<ApiResponse<*>> get() = _errorGetProjectSkills

    private val _errorDeleteSkillFromProject = MutableLiveData<ApiResponse<*>>()
    val errorDeleteSkillFromProject: LiveData<ApiResponse<*>> get() = _errorDeleteSkillFromProject

    private val _errorDeleteAllSkillsFromProject = MutableLiveData<ApiResponse<*>>()
    val errorDeleteAllSkillsFromProject: LiveData<ApiResponse<*>> get() = _errorDeleteAllSkillsFromProject

    fun getUserProfile(id: String) {
        _loadingUserProfile.value = true
        viewModelScope.launch {
            val response = profileRepository.getUserProfile(id)

            if (response.status == "fail" || response.status == "error") {
                _errorUserProfile.value = response
                _loadingUserProfile.value = false
                return@launch
            }

            val userProfile = response.data
            if (userProfile != null) {
                _userProfileResponse.value = userProfile!!
            }
            _loadingUserProfile.value = false
        }
    }

    fun updateUserProfile(id: String, request: UpdateUserProfileRequest) {
        _loadingUpdateUserProfile.value = true
        viewModelScope.launch {
            val response = profileRepository.updateUserProfile(id, request)

            if (response.status == "fail" || response.status == "error") {
                _errorUpdateUserProfile.value = response
                _loadingUpdateUserProfile.value = false
                return@launch
            }

            val updatedProfile = response.data

            if (updatedProfile != null) {
                _updateUserProfileResponse.value = updatedProfile!!
            }
            _loadingUpdateUserProfile.value = false
        }
    }

    fun deleteUserProfile(id: String) {
        _loadingDeleteUserProfile.value = true
        viewModelScope.launch {
            val response = profileRepository.deleteUserProfile(id)

            if (response.status == "fail" || response.status == "error") {
                _errorDeleteUserProfile.value = response
                _loadingDeleteUserProfile.value = false
                return@launch
            }

            _deleteUserProfileResponse.value = Unit
            _loadingDeleteUserProfile.value = false
        }
    }

    fun createProject(request: CreateProjectRequest) {
        _loadingCreateProject.value = true
        viewModelScope.launch {
            val response = profileRepository.createProject(request)

            if (response.status == "fail" || response.status == "error") {
                _errorCreateProject.value = response
                _loadingCreateProject.value = false
                return@launch
            }

            val createdProject = response.data

            if (createdProject != null) {
                _createProjectResponse.value = createdProject!!
            }

            _loadingCreateProject.value = false
        }
    }

    fun getUserProjects(profileId: String) {
        _loadingUserProjects.value = true
        viewModelScope.launch {
            val response = profileRepository.getUserProjects(profileId)

            if (response.status == "fail" || response.status == "error") {
                _errorUserProjects.value = response
                _loadingUserProjects.value = false
                return@launch
            }

            val projects = response.data

            if (projects != null) {
                _userProjectsResponse.value = projects!!
            }
            _loadingUserProjects.value = false
        }
    }

    fun getProjectDetails(id: String, projectId: String) {
        _loadingProjectDetails.value = true
        viewModelScope.launch {
            val response = profileRepository.getProjectDetails(id, projectId)

            if (response.status == "fail" || response.status == "error") {
                _errorProjectDetails.value = response
                _loadingProjectDetails.value = false
                return@launch
            }

            val projectDetails = response.data

            if (projectDetails != null) {
                _projectDetailsResponse.value = projectDetails!!
            }

            _loadingProjectDetails.value = false
        }
    }

    fun deleteProject(id: String, projectId: String) {
        _loadingDeleteProject.value = true
        viewModelScope.launch {
            val response = profileRepository.deleteProject(id, projectId)

            if (response.status == "fail" || response.status == "error") {
                _errorDeleteProject.value = response
                _loadingDeleteProject.value = false
                return@launch
            }

            _deleteProjectResponse.value = Unit
            _loadingDeleteProject.value = false
        }
    }

    fun updateProject(id: String, projectId: String, request: UpdateProjectRequest) {
        _loadingUpdateProject.value = true
        viewModelScope.launch {
            val response = profileRepository.updateProject(id, projectId, request)

            if (response.status == "fail" || response.status == "error") {
                _errorUpdateProject.value = response
                _loadingUpdateProject.value = false
                return@launch
            }

            val updatedProject = response.data

            if (updatedProject != null) {
                _updateProjectResponse.value = updatedProject!!
            }

            _loadingUpdateProject.value = false
        }
    }

    fun addProjectAttachment(id: String, projectId: String, request: AttachmentRequest) {
        _loadingAddProjectAttachment.value = true
        viewModelScope.launch {
            val response = profileRepository.addProjectAttachment(id, projectId, request)

            if (response.status == "fail" || response.status == "error") {
                _errorAddProjectAttachment.value = response
                _loadingAddProjectAttachment.value = false
                return@launch
            }

            val addedAttachment = response.data

            if (addedAttachment != null) {
                _addProjectAttachmentResponse.value = addedAttachment!!
            }
            _loadingAddProjectAttachment.value = false
        }
    }

    fun getProjectAttachments(id: String, projectId: String) {
        _loadingGetProjectAttachments.value = true
        viewModelScope.launch {
            val response = profileRepository.getProjectAttachments(id, projectId)

            if (response.status == "fail" || response.status == "error") {
                _errorGetProjectAttachments.value = response
                _loadingGetProjectAttachments.value = false
                return@launch
            }

            val attachments = response.data

            if (attachments != null) {
                _getProjectAttachmentsResponse.value = attachments!!
            }
            _loadingGetProjectAttachments.value = false
        }
    }

    fun getAttachmentDetails(id: String, projectId: String, attachmentId: String) {
        _loadingGetAttachmentDetails.value = true
        viewModelScope.launch {
            val response = profileRepository.getAttachmentDetails(id, projectId, attachmentId)

            if (response.status == "fail" || response.status == "error") {
                _errorGetAttachmentDetails.value = response
                _loadingGetAttachmentDetails.value = false
                return@launch
            }

            val attachmentDetails = response.data

            if (attachmentDetails != null) {
                _getAttachmentDetailsResponse.value = attachmentDetails!!
            }
            _loadingGetAttachmentDetails.value = false
        }
    }

    fun deleteAttachment(id: String, projectId: String, attachmentId: String) {
        _loadingDeleteAttachment.value = true
        viewModelScope.launch {
            val response = profileRepository.deleteAttachment(id, projectId, attachmentId)

            if (response.status == "fail" || response.status == "error") {
                _errorDeleteAttachment.value = response
                _loadingDeleteAttachment.value = false

                return@launch
            }

            _deleteAttachmentResponse.value = Unit
            _loadingDeleteAttachment.value = false

        }
    }

    fun deleteAllAttachments(id: String, projectId: String) {
        _loadingDeleteAllAttachments.value = true
        viewModelScope.launch {
            val response = profileRepository.deleteAllAttachments(id, projectId)

            if (response.status == "fail" || response.status == "error") {
                _errorDeleteAllAttachments.value = response
                _loadingDeleteAllAttachments.value = false

                return@launch
            }

            _deleteAllAttachmentsResponse.value = Unit
            _loadingDeleteAllAttachments.value = false

        }
    }

    fun addSkillsToProject(id: String, projectId: String, request: SkillsRequest) {
        _loadingAddSkillsToProject.value = true
        viewModelScope.launch {
            val response = profileRepository.addSkillsToProject(id, projectId, request)

            if (response.status == "fail" || response.status == "error") {
                _errorAddSkillsToProject.value = response
                _loadingAddSkillsToProject.value = false

                return@launch
            }

            _addSkillsToProjectResponse.value = Unit
            _loadingAddSkillsToProject.value = false

        }
    }

    fun getProjectSkills(id: String, projectId: String) {
        _loadingGetProjectSkills.value = true
        viewModelScope.launch {
            val response = profileRepository.getProjectSkills(id, projectId)

            if (response.status == "fail" || response.status == "error") {
                _errorGetProjectSkills.value = response
                _loadingGetProjectSkills.value = false

                return@launch
            }

            val skills = response.data

            if (skills != null) {
                _getProjectSkillsResponse.value = skills!!
            }
            _loadingGetProjectSkills.value = false
        }
    }

    fun deleteSkillFromProject(id: String, projectId: String, skillId: String) {
        _loadingDeleteSkillFromProject.value = true
        viewModelScope.launch {
            val response = profileRepository.deleteSkillFromProject(id, projectId, skillId)

            if (response.status == "fail" || response.status == "error") {
                _errorDeleteSkillFromProject.value = response
                _loadingDeleteSkillFromProject.value = false

                return@launch
            }

            _deleteSkillFromProjectResponse.value = Unit
            _loadingDeleteSkillFromProject.value = false

        }
    }

    fun deleteAllSkillsFromProject(id: String, projectId: String) {
        _loadingDeleteAllSkillsFromProject.value = true
        viewModelScope.launch {
            val response = profileRepository.deleteAllSkillsFromProject(id, projectId)

            if (response.status == "fail" || response.status == "error") {
                _errorDeleteAllSkillsFromProject.value = response
                _loadingDeleteAllSkillsFromProject.value = false

                return@launch
            }

            _deleteAllSkillsFromProjectResponse.value = Unit
            _loadingDeleteAllSkillsFromProject.value = false

        }
    }

}