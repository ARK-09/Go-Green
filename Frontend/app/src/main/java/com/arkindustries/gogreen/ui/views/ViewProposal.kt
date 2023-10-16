package com.arkindustries.gogreen.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.arkindustries.gogreen.AppContext
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.api.request.ReviewRequest
import com.arkindustries.gogreen.api.services.FileService
import com.arkindustries.gogreen.api.services.ProposalService
import com.arkindustries.gogreen.database.AppDatabase
import com.arkindustries.gogreen.database.dao.AttachmentDao
import com.arkindustries.gogreen.database.dao.ProposalDao
import com.arkindustries.gogreen.database.entites.AttachmentEntity
import com.arkindustries.gogreen.database.entites.ProposalEntity
import com.arkindustries.gogreen.databinding.ActivityViewProposalBinding
import com.arkindustries.gogreen.ui.adapters.ViewAttachmentsAdapter
import com.arkindustries.gogreen.ui.bindingadapters.location
import com.arkindustries.gogreen.ui.bindingadapters.timeAgo
import com.arkindustries.gogreen.ui.repositories.FileRepository
import com.arkindustries.gogreen.ui.repositories.ProposalRepository
import com.arkindustries.gogreen.ui.viewmodels.FileViewModel
import com.arkindustries.gogreen.ui.viewmodels.ProposalViewModel
import com.arkindustries.gogreen.ui.viewmodels.factory.ProposalViewModelFactory
import com.google.android.material.snackbar.Snackbar


class ViewProposal : Fragment(), FeedbackFragment.OnFeedbackListener {
    private lateinit var viewProposalBinding: ActivityViewProposalBinding
    private lateinit var fileService: FileService
    private lateinit var attachmentAdapter: ViewAttachmentsAdapter
    private lateinit var attachmentDao: AttachmentDao
    private lateinit var fileRepository: FileRepository
    private lateinit var fileViewModel: FileViewModel
    private lateinit var proposalService: ProposalService
    private lateinit var proposalDao: ProposalDao
    private lateinit var proposalRepository: ProposalRepository
    private lateinit var proposalViewModel: ProposalViewModel
    private lateinit var appDatabase: AppDatabase
    private var isUserClient: Boolean = false
    private lateinit var proposalId: String
    private var proposal: ProposalEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewProposalBinding = ActivityViewProposalBinding.inflate(inflater, container, false)
        return viewProposalBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewProposalBinding.lifecycleOwner = this

        isUserClient = AppContext.getInstance().currentUser.userType == "client"
        proposalId = ""

        if (isUserClient) {
            viewProposalBinding.proposalActionBtn.text = "Interview"
        }

        viewProposalBinding.proposalActionBtn.setOnClickListener {
            if(isUserClient && proposal != null) {
                when (proposal?.status) {
                    "Accepted" -> {
                        proposalViewModel.hireProposal (proposal!!.proposalId, proposal!!.doc)
                    }
                    "Submitted" -> {
                        proposalViewModel.createInterview(proposalId)
                    }
                    "Hired" -> {
                        val feedbackFragment = FeedbackFragment ()
                        feedbackFragment.show(childFragmentManager, FeedbackFragment::class.java.simpleName)
                    }
                }
            }
        }

        viewProposalBinding.proposedProposalDuration.isSelected = true

        appDatabase = AppDatabase.getInstance(requireContext())
        fileService = RetrofitClient.createFileService(requireContext())
        attachmentDao = appDatabase.attachmentDao()
        fileRepository = FileRepository(fileService, attachmentDao)
        fileViewModel = FileViewModel(fileRepository)

        proposalService = RetrofitClient.createProposalService(requireContext())
        proposalDao = appDatabase.proposalDao()
        proposalRepository = ProposalRepository(proposalDao, proposalService)

        proposalViewModel =
            ViewModelProvider(
                this,
                ProposalViewModelFactory(
                    proposalRepository,
                    fileRepository
                )
            )[ProposalViewModel::class.java]

        val onAttachmentClickListener = { _: Int, item: AttachmentEntity ->
            val intent = Intent(requireContext(), FileViewer::class.java)
            intent.putExtra("fileId", item.attachmentId)
            startActivity(intent)
        }

        val onAttachmentActionBtnClickListener = { _: Int, item: AttachmentEntity ->
            val intent = Intent(requireContext(), FileViewer::class.java)
            intent.putExtra("fileId", item.attachmentId)
            startActivity(intent)
        }

        attachmentAdapter =
            ViewAttachmentsAdapter(
                mutableListOf(),
                onAttachmentClickListener,
                onAttachmentActionBtnClickListener
            )

        viewProposalBinding.proposalAttachments.adapter = attachmentAdapter

        parentFragmentManager.setFragmentResultListener("viewProposal", this) { key, bundle ->
            proposalId = bundle.getString("proposalId").toString()
            proposalViewModel.getProposalById(proposalId)
        }

        getProposalObserver()
        getProposalLoadingObserver()
        createInterviewObserver ()
        createInterviewErrorObserver ()
        hireProposalObserver ()
        hireProposalErrorObserver ()
        feedbackObserver ()
        feedbackErrorObserver()
    }

    private fun getProposalObserver() {
        proposalViewModel.getProposalById.observe(viewLifecycleOwner) {
            proposal = it.proposal
            viewProposalBinding.jobTitleTv.text = it.job.title
            viewProposalBinding.jobDateTimeTv.timeAgo(it.job.createdDate)
            viewProposalBinding.jobLocationTv.location(it.job.location)

            viewProposalBinding.userName.text = if (isUserClient) {it.proposal.user?.name} else it.job.user?.name
            viewProposalBinding.proposalBidAmount.text = String.format("%s%.2f", resources.getString(R.string.currency)+" ", it.proposal.bidAmount)
            viewProposalBinding.proposedProposalDuration.text = it.proposal.proposedDuration
            viewProposalBinding.proposalStatus.text = it.proposal.status[0].uppercase()+it.proposal.status.substring(1)
            viewProposalBinding.proposalCoverLetter.text = it.proposal.coverLetter

            if (it.attachments.isEmpty()) {
                viewProposalBinding.noAttachment.visibility = View.VISIBLE
                viewProposalBinding.proposalAttachments.visibility = View.GONE
            } else {
                viewProposalBinding.noAttachment.visibility = View.GONE
                viewProposalBinding.proposalAttachments.visibility = View.VISIBLE
            }

            if (isUserClient && proposal?.status == "Accepted") {
                viewProposalBinding.proposalActionBtn.text = "Hire"

            } else if (isUserClient && proposal?.status == "Hired") {
                viewProposalBinding.proposalActionBtn.text = "Give Feedback"
            }

            viewProposalBinding.executePendingBindings()
        }
    }

    private fun getProposalLoadingObserver() {
        proposalViewModel.loadingState.observe(viewLifecycleOwner) {
            if (it != null) {
                viewProposalBinding.progressBar.visibility = if (it) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    private fun createInterviewObserver () {
        proposalViewModel.createInterview.observe(viewLifecycleOwner) {
            if (it != null) {
                val bundle = bundleOf("proposalId" to proposalId)
                setFragmentResult("createRoom", bundle)
                Navigation.findNavController(viewProposalBinding.proposalAttachments)
                    .navigate(R.id.action_viewProposal_to_rooms)
            }
        }
    }

    private fun createInterviewErrorObserver () {
        proposalViewModel.createInterviewError.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it.message!!, Toast.LENGTH_SHORT).show()
        }
    }

    private fun hireProposalObserver () {
        proposalViewModel.hireProposal.observe(viewLifecycleOwner) {
            Snackbar.make(viewProposalBinding.root, it?.message!!, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun hireProposalErrorObserver () {
        proposalViewModel.hireProposalError.observe(viewLifecycleOwner) {
            Snackbar.make(viewProposalBinding.root, it?.message!!, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun feedbackObserver () {
        proposalViewModel.createProposalFeedback.observe(viewLifecycleOwner) {
            Snackbar.make(viewProposalBinding.root, "Feedback created successfully", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun feedbackErrorObserver () {
        proposalViewModel.createProposalFeedbackError.observe(viewLifecycleOwner) {
            Snackbar.make(viewProposalBinding.root, it.message!!, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onFeedBack(feedback: ReviewRequest) {
        proposalViewModel.createProposalFeedback(proposalId, feedback)
    }
}