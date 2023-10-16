package com.arkindustries.gogreen.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkindustries.gogreen.api.request.CreateJobRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.AttachmentResponse
import com.arkindustries.gogreen.api.response.CategoriesResponse
import com.arkindustries.gogreen.api.response.Job
import com.arkindustries.gogreen.api.response.SkillsResponse
import com.arkindustries.gogreen.api.response.UserResponse
import com.arkindustries.gogreen.database.crossref.JobAttachmentCrossRef
import com.arkindustries.gogreen.database.crossref.JobCategoryCrossRef
import com.arkindustries.gogreen.database.crossref.JobSkillCrossRef
import com.arkindustries.gogreen.database.entites.AttachmentEntity
import com.arkindustries.gogreen.database.entites.CategoryEntity
import com.arkindustries.gogreen.database.entites.JobEntity
import com.arkindustries.gogreen.database.entites.JobWithCategoriesAndSkillsAndAttachments
import com.arkindustries.gogreen.database.entites.SkillEntity
import com.arkindustries.gogreen.database.entites.UserEntity
import com.arkindustries.gogreen.ui.repositories.CategoryRepository
import com.arkindustries.gogreen.ui.repositories.FileRepository
import com.arkindustries.gogreen.ui.repositories.JobRepository
import com.arkindustries.gogreen.ui.repositories.SkillRepository
import com.arkindustries.gogreen.ui.repositories.UserRepository
import kotlinx.coroutines.launch

class JobViewModel(
    private val jobRepository: JobRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val skillRepository: SkillRepository,
    private val fileRepository: FileRepository
) : ViewModel() {
    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> get() = _loadingState

    private val _jobsWithCategoriesAndSkillsAndAttachments =
        MutableLiveData<List<JobWithCategoriesAndSkillsAndAttachments>>()
    val jobsWithCategoriesAndSkillsAndAttachments: LiveData<List<JobWithCategoriesAndSkillsAndAttachments>> =
        _jobsWithCategoriesAndSkillsAndAttachments

    private val _searchJobsResult = MutableLiveData<List<JobEntity>>()
    val searchJobsResult: LiveData<List<JobEntity>> = _searchJobsResult

    private val _jobs = MutableLiveData<List<JobEntity>>()
    val jobs: LiveData<List<JobEntity>> = _jobs

    private val _job = MutableLiveData<JobWithCategoriesAndSkillsAndAttachments>()
    val job: LiveData<JobWithCategoriesAndSkillsAndAttachments> = _job

    private val _error = MutableLiveData<ApiResponse<*>>()
    val error: LiveData<ApiResponse<*>> = _error

    fun searchJobs(
        query: String?,
        offset: Int?,
        limit: Int?,
        latitude: Double?,
        longitude: Double?,
        price: String?
    ) {
        _loadingState.value = true
        viewModelScope.launch {
            val response =
                jobRepository.searchJobs(query, offset, limit, latitude, longitude, price)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loadingState.value = false
                return@launch
            }

            if (response.status === "success") {
                response.data?.jobs?.let { upsertJobs(it) }
                _searchJobsResult.value = jobRepository.getJobsFromLocalServer()
            }
            _loadingState.value = false
        }
    }

    fun createJob(jobRequest: CreateJobRequest) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = jobRepository.createJob(jobRequest)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loadingState.value = false
                return@launch
            }

            if (response.data != null) {
                upsertJobs(mutableListOf(response.data.job))
                _job.value =
                    jobRepository.getJobWithCategoriesAndSkillsAndAttachmentsFromLocal(response.data.job._id)
            }

            _loadingState.value = false
        }
    }

    fun getJobs(userId: String) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = jobRepository.getJobsByCurrentUserFromServer()

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loadingState.value = false
                return@launch
            }

            if (response.data?.jobs != null) {
                upsertJobs(response.data.jobs)
                _jobs.value = jobRepository.getJobsByCurrentUser(userId)
            }

            _loadingState.value = false
        }
    }

    fun getLocalJobsWithCategoriesAndSkillsAndAttachments() {
        _loadingState.value = true
        viewModelScope.launch {
            _jobsWithCategoriesAndSkillsAndAttachments.value =
                jobRepository.getJobsWithCategoriesAndSkillsAndAttachmentsFromLocal()
            _loadingState.value = false
        }
    }

    fun getJobById(id: String) {
        _loadingState.value = true
        viewModelScope.launch {
            val jobResponse = jobRepository.getJobByIdFromServer(id)

            if (jobResponse.data != null) {
                upsertJobs(mutableListOf(jobResponse.data.job))
                _job.value =
                    jobRepository.getJobWithCategoriesAndSkillsAndAttachmentsFromLocal(id)
            }
            _loadingState.value = false
        }
    }

    fun updateJob(jobId: String, jobRequest: CreateJobRequest) {
        _loadingState.value = true
        viewModelScope.launch {
            val updateJobResponse = jobRepository.updateJobAtServer(jobId, jobRequest)
            if (updateJobResponse.data != null) {
                upsertJobs(listOf(updateJobResponse.data.job))
                _job.value =
                    jobRepository.getJobWithCategoriesAndSkillsAndAttachmentsFromLocal(jobId)
            }
            _loadingState.value = false
        }

    }

    private suspend fun upsertUserFromResponse(response: List<UserResponse>) {
        val user = response.map {
            val user = it.user
            return@map UserEntity(
                user._id,
                user.name,
                user.email,
                user.phoneNo,
                user.userType,
                user.image,
                user.userStatus,
                user.verified,
                user.financeAllowed,
                user.blocked.isBlocked,
                user.blocked.reason,
                user.joinedDate
            )
        }
        userRepository.upsertUsersToLocal(user)
    }

    private suspend fun upsertCategoriesFromResponse(
        jobId: String,
        categories: CategoriesResponse
    ) {
        val jobWithCategoriesCrossRef = mutableListOf<JobCategoryCrossRef>()

        val categoriesEntities = categories.categories.map { category ->
            jobWithCategoriesCrossRef.add(JobCategoryCrossRef(jobId, category._id))
            return@map CategoryEntity(category._id, category.title)
        }

        jobRepository.upsertJobWithCategories(jobWithCategoriesCrossRef)
        categoryRepository.upsertCategoriesToLocal(categoriesEntities)
    }

    private suspend fun upsertSkillsFromResponse(jobId: String, skills: SkillsResponse) {
        val jobWithSkillCrossRef = mutableListOf<JobSkillCrossRef>()

        val skillsEntities = skills.skills.map { skill ->
            jobWithSkillCrossRef.add(JobSkillCrossRef(jobId, skill._id))
            return@map SkillEntity(skill._id, skill.title)
        }

        jobRepository.upsertJobWithSkills(jobWithSkillCrossRef)
        skillRepository.upsertSkillsToLocal(skillsEntities)
    }

    private suspend fun upsertAttachmentsFromResponse(attachments: List<AttachmentResponse>) {
        val attachmentsEntities = attachments.map { attachment ->
            return@map AttachmentEntity(
                attachment.id,
                attachment.mimeType,
                attachment.originalName,
                attachment.createdDate
            )
        }
        fileRepository.upsertFilesAtLocal(attachmentsEntities)
    }

    private suspend fun upsertJobWithAttachments(jobId: String, attachments: List<AttachmentResponse>) {
        val jobWithAttachmentsEntities = attachments.map { attachment ->
            return@map JobAttachmentCrossRef(jobId, attachment.id)
        }
        jobRepository.upsertJobWithAttachments(jobWithAttachmentsEntities)
    }

    private suspend fun upsertJobs(jobResponse: List<Job>) {
        jobResponse.map { job ->
            val jobs = JobEntity(
                job._id,
                job.title,
                job.description,
                job.budget,
                job.status,
                job.expectedDuration,
                job.paymentType,
                job.location,
                job.createdDate,
                user = UserEntity(
                    job.user._id,
                    job.user.name,
                    job.user.email,
                    job.user.phoneNo,
                    job.user.userType,
                    job.user.image,
                    job.user.userStatus,
                    job.user.verified,
                    job.user.financeAllowed,
                    job.user.blocked.isBlocked,
                    job.user.blocked.reason,
                    job.user.joinedDate
                ),
                job.noOfProposals,
                job.interviewing
            )

            val user = job.user
            upsertUserFromResponse(mutableListOf(UserResponse(user)))

            val categories = job.categories
            if (categories != null) {
                upsertCategoriesFromResponse(job._id, CategoriesResponse(categories))
            }

            val skills = job.skills
            if (skills != null) {
                upsertSkillsFromResponse(job._id, SkillsResponse(skills))
            }

            val attachments = job.attachments
            if (attachments != null) {
                upsertJobWithAttachments(job._id, attachments);
                upsertAttachmentsFromResponse(attachments)
            }

            jobRepository.upsertJobs(mutableListOf(jobs))
        }
    }
}
