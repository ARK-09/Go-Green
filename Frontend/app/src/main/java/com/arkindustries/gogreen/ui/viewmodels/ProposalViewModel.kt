package com.arkindustries.gogreen.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkindustries.gogreen.api.request.AttachmentRequest
import com.arkindustries.gogreen.api.request.CreateJobProposalRequest
import com.arkindustries.gogreen.api.request.ReviewRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.AttachmentResponse
import com.arkindustries.gogreen.api.response.CreateInterviewResponse
import com.arkindustries.gogreen.api.response.HireProposalResponse
import com.arkindustries.gogreen.api.response.Proposal
import com.arkindustries.gogreen.api.response.ProposalResponse
import com.arkindustries.gogreen.api.response.ProposalsResponse
import com.arkindustries.gogreen.api.response.ReviewsResponse
import com.arkindustries.gogreen.database.crossref.ProposalJobCrossRef
import com.arkindustries.gogreen.database.entites.AttachmentEntity
import com.arkindustries.gogreen.database.entites.ProposalEntity
import com.arkindustries.gogreen.database.entites.ProposalWithAttachments
import com.arkindustries.gogreen.database.entites.ProposalWithAttachmentsAndJob
import com.arkindustries.gogreen.database.entites.UserEntity
import com.arkindustries.gogreen.ui.repositories.FileRepository
import com.arkindustries.gogreen.ui.repositories.ProposalRepository
import kotlinx.coroutines.launch

class ProposalViewModel(
    private val proposalRepository: ProposalRepository, private val fileRepository: FileRepository
) : ViewModel() {
    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> get() = _loadingState

    private val _error = MutableLiveData<ApiResponse<*>>()
    val error: LiveData<ApiResponse<*>> = _error

    private val _createJobProposal = MutableLiveData<Unit>()
    val createJobProposal: LiveData<Unit> = _createJobProposal

    private val _createInterview = MutableLiveData<CreateInterviewResponse?>()
    val createInterview: LiveData<CreateInterviewResponse?> = _createInterview

    private val _hireProposal = MutableLiveData<HireProposalResponse?>()
    val hireProposal: LiveData<HireProposalResponse?> = _hireProposal

    private val _getProposalFeedbacks = MutableLiveData<ReviewsResponse>()
    val getProposalFeedbacks: LiveData<ReviewsResponse> = _getProposalFeedbacks

    private val _createProposalFeedback = MutableLiveData<ProposalResponse>()
    val createProposalFeedback: LiveData<ProposalResponse> = _createProposalFeedback

    private val _getJobProposals = MutableLiveData<List<ProposalWithAttachments>>()
    val getJobProposals: LiveData<List<ProposalWithAttachments>> = _getJobProposals

    private val _getJobProposalsWithAttachmentsAndJob =
        MutableLiveData<List<ProposalWithAttachmentsAndJob>>()
    val getJobProposalsWithAttachmentsAndJob: LiveData<List<ProposalWithAttachmentsAndJob>> =
        _getJobProposalsWithAttachmentsAndJob

    private val _getProposalByUser = MutableLiveData<List<ProposalWithAttachmentsAndJob>>()
    val getProposalByUser: LiveData<List<ProposalWithAttachmentsAndJob>> = _getProposalByUser

    private val _createServiceProposal = MutableLiveData<ApiResponse<Unit>>()
    val createServiceProposal: LiveData<ApiResponse<Unit>> = _createServiceProposal

    private val _getServiceProposals = MutableLiveData<List<ProposalWithAttachments>>()
    val getServiceProposals: LiveData<List<ProposalWithAttachments>> = _getServiceProposals

    private val _getProposalById = MutableLiveData<ProposalWithAttachmentsAndJob>()
    val getProposalById: LiveData<ProposalWithAttachmentsAndJob> = _getProposalById

    private val _deleteProposal = MutableLiveData<Unit>()
    val deleteProposal: LiveData<Unit> = _deleteProposal

    private val _addJobProposalAttachment = MutableLiveData<Unit>()
    val addJobProposalAttachment: LiveData<Unit> = _addJobProposalAttachment

    private val _getJobProposalAttachments = MutableLiveData<List<AttachmentEntity>>()
    val getJobProposalAttachments: LiveData<List<AttachmentEntity>> = _getJobProposalAttachments

    private val _getJobProposalAttachmentById = MutableLiveData<AttachmentEntity>()
    val getJobProposalAttachmentById: LiveData<AttachmentEntity> = _getJobProposalAttachmentById

    private val _deleteJobProposalAttachment = MutableLiveData<Unit>()
    val deleteJobProposalAttachment: LiveData<Unit> = _deleteJobProposalAttachment

    private val _addProposalAttachment = MutableLiveData<Unit>()
    val addProposalAttachment: LiveData<Unit> = _addProposalAttachment

    private val _getProposalAttachments = MutableLiveData<List<AttachmentEntity>?>()
    val getProposalAttachments: LiveData<List<AttachmentEntity>?> = _getProposalAttachments

    private val _getProposalAttachmentById = MutableLiveData<AttachmentEntity>()
    val getProposalAttachmentById: LiveData<AttachmentEntity> = _getProposalAttachmentById

    private val _deleteProposalAttachment = MutableLiveData<Unit>()
    val deleteProposalAttachment: LiveData<Unit> = _deleteProposalAttachment

    private val _createJobProposalError = MutableLiveData<ApiResponse<*>>()
    val createJobProposalError: LiveData<ApiResponse<*>> = _createJobProposalError

    private val _createInterviewError = MutableLiveData<ApiResponse<*>>()
    val createInterviewError: LiveData<ApiResponse<*>> = _createInterviewError

    private val _hireProposalError = MutableLiveData<ApiResponse<*>>()
    val hireProposalError: LiveData<ApiResponse<*>> = _hireProposalError

    private val _getProposalFeedbacksError = MutableLiveData<ApiResponse<*>>()
    val getProposalFeedbacksError: LiveData<ApiResponse<*>> = _getProposalFeedbacksError

    private val _createProposalFeedbackError = MutableLiveData<ApiResponse<*>>()
    val createProposalFeedbackError: LiveData<ApiResponse<*>> = _createProposalFeedbackError

    private val _getJobProposalWithAttachmentsAndUserError = MutableLiveData<ApiResponse<*>>()
    val getJobProposalWithAttachmentsAndUserError: LiveData<ApiResponse<*>> =
        _getJobProposalWithAttachmentsAndUserError

    private val _getProposalByUserError = MutableLiveData<ApiResponse<*>>()
    val getProposalByUserError: LiveData<ApiResponse<*>> = _getProposalByUserError

    private val _createServiceProposalError = MutableLiveData<ApiResponse<*>>()
    val createServiceProposalError: LiveData<ApiResponse<*>> = _createServiceProposalError

    private val _getServiceProposalError = MutableLiveData<ApiResponse<*>>()
    val getServiceProposalError: LiveData<ApiResponse<*>> = _getServiceProposalError

    private val _getProposalByIdError = MutableLiveData<ApiResponse<*>>()
    val getProposalByIdError: LiveData<ApiResponse<*>> = _getProposalByIdError

    private val _deleteProposalError = MutableLiveData<ApiResponse<*>>()
    val deleteProposalError: LiveData<ApiResponse<*>> = _deleteProposalError

    private val _addJobProposalAttachmentError = MutableLiveData<ApiResponse<*>>()
    val addJobProposalAttachmentError: LiveData<ApiResponse<*>> = _addJobProposalAttachmentError

    private val _getJobProposalAttachmentsError = MutableLiveData<ApiResponse<*>>()
    val getJobProposalAttachmentsError: LiveData<ApiResponse<*>> = _getJobProposalAttachmentsError

    private val _getJobProposalAttachmentByIdError = MutableLiveData<ApiResponse<*>>()
    val getJobProposalAttachmentByIdError: LiveData<ApiResponse<*>> =
        _getJobProposalAttachmentByIdError

    private val _deleteJobProposalAttachmentError = MutableLiveData<ApiResponse<*>>()
    val deleteJobProposalAttachmentError: LiveData<ApiResponse<*>> =
        _deleteJobProposalAttachmentError

    private val _addProposalAttachmentError = MutableLiveData<ApiResponse<*>>()
    val addProposalAttachmentError: LiveData<ApiResponse<*>> = _addProposalAttachmentError

    private val _getProposalAttachmentsError = MutableLiveData<ApiResponse<*>>()
    val getProposalAttachmentsError: LiveData<ApiResponse<*>> = _getProposalAttachmentsError

    private val _getProposalAttachmentByIdError = MutableLiveData<ApiResponse<*>>()
    val getProposalAttachmentByIdError: LiveData<ApiResponse<*>> = _getProposalAttachmentByIdError

    private val _deleteProposalAttachmentError = MutableLiveData<ApiResponse<*>>()
    val deleteProposalAttachmentError: LiveData<ApiResponse<*>> = _deleteProposalAttachmentError

    fun createJobProposal(
        jobId: String, request: CreateJobProposalRequest
    ) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = proposalRepository.createJobProposalAtServer(jobId, request)

            if (response.status == "fail" || response.status == "error") {
                _createJobProposalError.value = response
                return@launch
            }

            val proposal = response.data

            if (proposal != null) {
                upsertProposalEntitiesFromResponse(ProposalsResponse(mutableListOf(proposal.proposal)))
                _createJobProposal.value = Unit
            }
            _loadingState.value = false
        }
    }

    fun createProposalFeedback(
        proposalId: String, request: ReviewRequest
    ) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = proposalRepository.createProposalFeedback(proposalId, request)

            if (response.status == "fail" || response.status == "error") {
                _createProposalFeedbackError.value = response
                return@launch
            }

            val proposal = response.data

            if (proposal != null) {
                upsertProposalEntitiesFromResponse(ProposalsResponse(mutableListOf(proposal.proposal)))
                _createProposalFeedback.value = response.data!!
            }
            _loadingState.value = false
        }
    }

    fun getJobProposals(jobId: String) {
        _loadingState.value = true
        viewModelScope.launch {
            _getJobProposalsWithAttachmentsAndJob.value =
                proposalRepository.getJobProposalsWithAttachmentsAndJobFromLocal(jobId)
            val response = proposalRepository.getJobProposalsFromServer(jobId)

            if (response.status == "fail" || response.status == "error") {
                _getJobProposalWithAttachmentsAndUserError.value = response
                return@launch
            }

            val proposals = response.data

            if (proposals != null) {
                upsertProposalEntitiesFromResponse(proposals)
                _getJobProposalsWithAttachmentsAndJob.value =
                    proposalRepository.getJobProposalsWithAttachmentsAndJobFromLocal(jobId)
            }
            _loadingState.value = false
        }
    }

    fun getProposalFeedbacks(userId: String) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = proposalRepository.getUserReviews(userId)

            if (response.status == "fail" || response.status == "error") {
                _getProposalFeedbacksError.value = response
                return@launch
            }

            val proposals = response.data

            if (proposals != null) {
                _getProposalFeedbacks.value = response.data!!
            }
            _loadingState.value = false
        }
    }

    fun getProposalsByUser(userId: String) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = proposalRepository.getProposalsByUserFromServer()

            if (response.status == "fail" || response.status == "error") {
                _getProposalByIdError.value = response
                _loadingState.value = false
                return@launch
            }

            val proposals = response.data

            if (proposals != null) {
                _loadingState.value = true
                proposalRepository.deleteAllFromLocal()
                proposalRepository.deleteAllProposalUserCrossRefFromLocal()
                proposalRepository.deleteAllProposalUserCrossRefFromLocal()
                upsertProposalEntitiesFromResponse(proposals)
                _getProposalByUser.value = proposalRepository.getProposalsByUserFromLocal(userId)
            }
            _loadingState.value = false
        }
    }


    fun createServiceProposal(
        serviceId: String, request: CreateJobProposalRequest
    ) {
        // TODO ("Not yet implemented")
    }

    fun getServiceProposals(serviceId: String) {
        // TODO ("Not yet implemented")
    }

    fun getProposalById(proposalId: String) {
        _loadingState.value = true
        viewModelScope.launch {
            _getProposalById.value = proposalRepository.getProposalByIdFromLocal(proposalId)

            val response = proposalRepository.getProposalByIdFromServer(proposalId)

            if (response.status == "fail" || response.status == "error") {
                _getProposalByIdError.value = response
                _loadingState.value = false
                return@launch
            }

            val proposal = response.data

            if (proposal != null) {
                upsertProposalEntitiesFromResponse(ProposalsResponse(mutableListOf(proposal.proposal)))
                _getProposalById.value = proposalRepository.getProposalByIdFromLocal(proposalId)
            }
            _loadingState.value = false
        }
    }

    fun createInterview(proposalId: String) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = proposalRepository.createInterview(proposalId)

            if (response.status == "fail" || response.status == "error") {
                _createInterviewError.value = response
                _loadingState.value = false
                return@launch
            }

            _createInterview.value = response.data
            _loadingState.value = false
        }
    }

    fun hireProposal(proposalId: String, jobId: String) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = proposalRepository.hireProposal(proposalId, jobId)

            if (response.status == "fail" || response.status == "error") {
                _hireProposalError.value = response
                _loadingState.value = false
                return@launch
            }

            _hireProposal.value = response.data
            _loadingState.value = false
        }
    }

    fun deleteProposal(proposalId: String) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = proposalRepository.deleteProposalFromServer(proposalId)

            if (response.status == "fail" || response.status == "error") {
                _deleteProposalError.value = response
                _loadingState.value = false
                return@launch
            }

            proposalRepository.deleteByIdFromLocal(proposalId)
            _deleteProposal.value = Unit
            _loadingState.value = false
        }
    }

    fun addProposalAttachments(
        proposalId: String, request: List<AttachmentRequest>
    ) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = proposalRepository.addProposalAttachmentAtServer(proposalId, request)

            if (response.status == "fail" || response.status == "error") {
                _addProposalAttachmentError.value = response
                _loadingState.value = false
                return@launch
            }

            getProposalById(proposalId)
            _loadingState.value = false
        }
    }

    fun getProposalAttachments(proposalId: String) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = proposalRepository.getProposalAttachmentsFromServer(proposalId)

            if (response.status == "fail" || response.status == "error") {
                _getProposalAttachmentsError.value = response
                _loadingState.value = false
                return@launch
            }

            getProposalById(proposalId)
            _loadingState.value = false
        }
    }

    fun getProposalAttachment(
        proposalId: String, attachmentId: String
    ) {
        _loadingState.value = true
        viewModelScope.launch {
            val response =
                proposalRepository.getProposalAttachmentByIdFromServer(proposalId, attachmentId)

            if (response.status == "fail" || response.status == "error") {
                _getProposalAttachmentByIdError.value = response
                _loadingState.value = false
                return@launch
            }

            getProposalById(proposalId)
            _loadingState.value = false
        }
    }

    fun deleteProposalAttachment(
        proposalId: String, attachmentId: String
    ) {
        _loadingState.value = true
        viewModelScope.launch {
            val response =
                proposalRepository.deleteJobProposalAttachmentFromServer(proposalId, attachmentId)

            if (response.status == "fail" || response.status == "error") {
                _createJobProposalError.value = response
                _loadingState.value = false
                return@launch
            }
            fileRepository.deleteFileByIdFromLocal(attachmentId)
            getProposalById(proposalId)
            _loadingState.value = false
        }
    }

    fun getProposalAttachmentById(
        proposalId: String, attachmentId: String
    ) {
        _loadingState.value = true
        viewModelScope.launch {
            val response =
                proposalRepository.getProposalAttachmentByIdFromServer(proposalId, attachmentId)

            if (response.status == "fail" || response.status == "error") {
                _getProposalAttachmentByIdError.value = response
                _loadingState.value = false
                return@launch
            }

            val attachment = response.data

            if (attachment != null) {
                val attachmentEntity = AttachmentEntity(
                    attachment.id,
                    attachment.mimeType,
                    attachment.originalName,
                    attachment.createdDate,
                    attachment.url
                )
                fileRepository.upsertFilesAtLocal(mutableListOf(attachmentEntity))
                _getProposalAttachmentById.value = attachmentEntity
            }
            _loadingState.value = false
        }
    }

    private suspend fun upsertProposalEntitiesFromResponse(proposals: ProposalsResponse) {
        val proposalWithJob = mutableListOf<Proposal>()
        val proposalEntities = proposals.proposals.map { proposal ->
            proposal.attachments?.let { upsertProposalAttachments(it) }

            if (proposal.job != null) {
                proposalWithJob.add(proposal)
            }

            return@map ProposalEntity(
                proposal._id,
                proposal.doc,
                proposal.type,
                proposal.bidAmount,
                proposal.status,
                proposal.coverLetter,
                proposal.proposedDuration,
                proposal.clientFeedback,
                proposal.talentFeedback,
                proposal.clientRating,
                proposal.talentRating,
                UserEntity(
                    proposal.user._id,
                    proposal.user.name,
                    proposal.user.email,
                    null,
                    proposal.user.userType,
                    proposal.user.image,
                    proposal.user.userStatus,
                    proposal.user.verified,
                    null,
                    false,
                    null,
                    null
                ),
            )
        }
        proposalRepository.upsertProposalsAtLocal(proposalEntities)
        if (proposalWithJob.isNotEmpty()) {
            upsertProposalWithJob(proposalWithJob)
        }
    }

    private suspend fun upsertProposalAttachments(attachments: List<AttachmentResponse>) {
        val attachmentEntities = attachments.map attachmentsIdsMap@{
            return@attachmentsIdsMap AttachmentEntity(
                it.id, it.mimeType, it.originalName, it.createdDate, it.url
            )
        }

        fileRepository.upsertFilesAtLocal(attachmentEntities)
    }

    private suspend fun upsertProposalWithJob(proposals: List<Proposal>) {
        val proposalJobCrossRefList = proposals.map { proposal ->
            return@map ProposalJobCrossRef(proposal._id, proposal.job!!._id)
        }

        proposalRepository.upsertProposalWithJobAtLocal(proposalJobCrossRefList)
    }
}