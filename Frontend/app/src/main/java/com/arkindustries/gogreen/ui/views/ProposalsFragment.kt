package com.arkindustries.gogreen.ui.views

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
import androidx.transition.TransitionInflater
import com.arkindustries.gogreen.AppContext
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.api.services.CategoryService
import com.arkindustries.gogreen.api.services.FileService
import com.arkindustries.gogreen.api.services.JobService
import com.arkindustries.gogreen.api.services.ProposalService
import com.arkindustries.gogreen.api.services.SkillService
import com.arkindustries.gogreen.api.services.UserService
import com.arkindustries.gogreen.database.AppDatabase
import com.arkindustries.gogreen.database.dao.AttachmentDao
import com.arkindustries.gogreen.database.dao.ProposalDao
import com.arkindustries.gogreen.database.entites.ProposalWithAttachmentsAndJob
import com.arkindustries.gogreen.databinding.FragmentProposalsBinding
import com.arkindustries.gogreen.ui.adapters.ProposalsAdapter
import com.arkindustries.gogreen.ui.repositories.CategoryRepository
import com.arkindustries.gogreen.ui.repositories.FileRepository
import com.arkindustries.gogreen.ui.repositories.JobRepository
import com.arkindustries.gogreen.ui.repositories.ProposalRepository
import com.arkindustries.gogreen.ui.repositories.SkillRepository
import com.arkindustries.gogreen.ui.repositories.UserRepository
import com.arkindustries.gogreen.ui.viewmodels.FileViewModel
import com.arkindustries.gogreen.ui.viewmodels.JobViewModel
import com.arkindustries.gogreen.ui.viewmodels.ProposalViewModel
import com.arkindustries.gogreen.ui.viewmodels.factory.JobViewModelFactory
import com.arkindustries.gogreen.ui.viewmodels.factory.ProposalViewModelFactory

class ProposalsFragment : Fragment() {
    private lateinit var proposalsBinding: FragmentProposalsBinding
    private lateinit var jobViewModel: JobViewModel
    private lateinit var fileService: FileService
    private lateinit var database: AppDatabase
    private lateinit var jobService: JobService
    private lateinit var userService: UserService
    private lateinit var categoryService: CategoryService
    private lateinit var skillService: SkillService
    private lateinit var jobRepository: JobRepository
    private lateinit var userRepository: UserRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var skillRepository: SkillRepository
    private lateinit var attachmentDao: AttachmentDao
    private lateinit var fileRepository: FileRepository
    private lateinit var fileViewModel: FileViewModel
    private lateinit var proposalService: ProposalService
    private lateinit var proposalDao: ProposalDao
    private lateinit var proposalRepository: ProposalRepository
    private lateinit var proposalViewModel: ProposalViewModel
    private lateinit var proposalsAdapter: ProposalsAdapter
    private var isUserClient: Boolean = false
    private lateinit var proposalId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isUserClient = AppContext.getInstance().currentUser.userType == "client"
        proposalId = ""

        database = AppDatabase.getInstance(requireContext())
        jobService = RetrofitClient.createJobService(requireContext())
        userService = RetrofitClient.createUserService(requireContext())
        categoryService = RetrofitClient.createCategoryService(requireContext())
        skillService = RetrofitClient.createSkillService(requireContext())
        fileService = RetrofitClient.createFileService(requireContext())

        jobRepository = JobRepository(database.jobDao(), jobService)
        userRepository = UserRepository(userService, database.userDao())
        categoryRepository = CategoryRepository(categoryService, database.categoryDao())
        skillRepository = SkillRepository(skillService, database.skillDao())

        attachmentDao = database.attachmentDao()
        fileRepository = FileRepository(fileService, attachmentDao)
        fileViewModel = FileViewModel(fileRepository)

        proposalService = RetrofitClient.createProposalService(requireContext())
        proposalDao = database.proposalDao()
        proposalRepository = ProposalRepository(proposalDao, proposalService)
        proposalViewModel = ProposalViewModel(proposalRepository, fileRepository)
        jobRepository = JobRepository(database.jobDao(), jobService)
        userRepository = UserRepository(userService, database.userDao())
        categoryRepository = CategoryRepository(categoryService, database.categoryDao())
        skillRepository = SkillRepository(skillService, database.skillDao())

        jobViewModel =
            ViewModelProvider(
                this,
                JobViewModelFactory(
                    jobRepository,
                    userRepository,
                    categoryRepository,
                    skillRepository,
                    fileRepository
                )
            )[JobViewModel::class.java]
        proposalViewModel =
            ViewModelProvider(
                this,
                ProposalViewModelFactory(
                    proposalRepository,
                    fileRepository
                )
            )[ProposalViewModel::class.java]

        val onItemClickListener = { proposal: ProposalWithAttachmentsAndJob ->
            val bundle = bundleOf("proposalId" to proposal.proposal.proposalId)
            parentFragmentManager.setFragmentResult("viewProposal", bundle)
            Navigation.findNavController(proposalsBinding.proposalsRv)
                .navigate(R.id.action_proposalsFragment_to_viewProposal)
        }

        val onOfferClickListener = { proposal: ProposalWithAttachmentsAndJob ->
            if (isUserClient) {
                proposalId = proposal.proposal.proposalId
                proposalViewModel.createInterview(proposalId)
            }
        }

        proposalsAdapter = ProposalsAdapter(onItemClickListener, onOfferClickListener, isUserClient)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        proposalsBinding = FragmentProposalsBinding.inflate(inflater)
        enterTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.slide_out)
        exitTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.slide_in)
        return proposalsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        proposalsBinding.proposalsRv.adapter = proposalsAdapter

        if (isUserClient) {
            jobsObserver()
            jobProposalObserver()
        } else {
            proposalViewModel.getProposalsByUser(AppContext.getInstance().currentUser.userId)
            proposalsObserver()
        }
        proposalsLoadingObserver()
        createInterviewObserver()
        createInterviewErrorObserver()
    }

    private fun proposalsLoadingObserver() {
        proposalViewModel.loadingState.observe(viewLifecycleOwner) {
            proposalsBinding.progress.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }

            if (!it) {
                if (proposalsAdapter.proposalWithAttachmentsAndJob.isEmpty()) {
                    proposalsBinding.progress.visibility = View.INVISIBLE
                    proposalsBinding.proposalsRv.visibility = View.INVISIBLE
                    if (!isUserClient) {
                        proposalsBinding.noProposals.text =
                            "Yet not applied for any job. Go Apply and check back."
                    }
                    proposalsBinding.noProposals.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun jobsObserver() {
        jobViewModel.getJobs(AppContext.getInstance().currentUser.userId)
        jobViewModel.jobs.observe(viewLifecycleOwner) {
            var noProposalsCounter = 0
            if (!it.isNullOrEmpty()) {
                it.forEach { jobEntity ->
                    if (jobEntity.noOfProposals > 0) {
                        proposalViewModel.getJobProposals(jobEntity.jobId)
                    } else {
                        noProposalsCounter++
                    }
                }

                if (noProposalsCounter == it.size) {
                    proposalsBinding.progress.visibility = View.INVISIBLE
                    proposalsBinding.proposalsRv.visibility = View.INVISIBLE
                    proposalsBinding.noProposals.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun jobProposalObserver() {
        proposalViewModel.getJobProposalsWithAttachmentsAndJob.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                proposalsAdapter.appendList(it)
            }
        }
    }

    private fun proposalsObserver() {
        proposalViewModel.getProposalByUser.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                proposalsAdapter.submitList(it)
            }
        }
    }

    private fun createInterviewObserver() {
        proposalViewModel.createInterview.observe(viewLifecycleOwner) {
            if (it != null) {
                val bundle = bundleOf("proposalId" to proposalId)
                setFragmentResult("createRoom", bundle)
                Navigation.findNavController(proposalsBinding.proposalsRv)
                    .navigate(R.id.action_viewProposal_to_rooms)
            }
        }
    }

    private fun createInterviewErrorObserver() {
        proposalViewModel.createInterviewError.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it.message!!, Toast.LENGTH_SHORT).show()
        }
    }
}