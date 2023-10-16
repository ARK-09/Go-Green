package com.arkindustries.gogreen.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.arkindustries.gogreen.AppContext
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.api.services.CategoryService
import com.arkindustries.gogreen.api.services.FileService
import com.arkindustries.gogreen.api.services.JobService
import com.arkindustries.gogreen.api.services.SkillService
import com.arkindustries.gogreen.api.services.UserService
import com.arkindustries.gogreen.database.AppDatabase
import com.arkindustries.gogreen.database.entites.AttachmentEntity
import com.arkindustries.gogreen.databinding.ActivityViewJobBinding
import com.arkindustries.gogreen.ui.adapters.ViewAttachmentsAdapter
import com.arkindustries.gogreen.ui.repositories.AttachmentRepository
import com.arkindustries.gogreen.ui.repositories.CategoryRepository
import com.arkindustries.gogreen.ui.repositories.JobRepository
import com.arkindustries.gogreen.ui.repositories.SkillRepository
import com.arkindustries.gogreen.ui.repositories.UserRepository
import com.arkindustries.gogreen.ui.viewmodels.JobViewModel
import com.arkindustries.gogreen.ui.viewmodels.factory.JobViewModelFactory


class ViewJob : Fragment() {
    private lateinit var viewJobBinding: ActivityViewJobBinding
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
    private lateinit var attachmentRepository: AttachmentRepository
    private lateinit var attachmentAdapter: ViewAttachmentsAdapter
    private var isUserClient: Boolean = false
    private lateinit var jobId: String

    //    private val args: ViewJobArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewJobBinding = ActivityViewJobBinding.inflate(inflater)
        return viewJobBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewJobBinding.lifecycleOwner = this

        setFragmentResultListener("viewJob") { key, bundle ->
            jobId = bundle.getString("jobId")!!
            jobViewModel.getJobById(jobId)
            getJobObserver()
            getJobLoadingObserver()
        }

        isUserClient = AppContext.getInstance().currentUser.userType == "client"

        if (isUserClient) {
            viewJobBinding.jobActionBtn.text = "Edit Job"
        }

        viewJobBinding.jobActionBtn.setOnClickListener {
            if (!isUserClient) {
                val bundle = bundleOf("jobId" to jobId)
                setFragmentResult("proposalForJobId", bundle)
                viewJobBinding.root.findNavController().navigate(R.id.action_viewJob_to_proposal)
            }
        }

        viewJobBinding.jobDuration.isSelected = true

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
        attachmentRepository = AttachmentRepository(fileService, database.attachmentDao())

        jobViewModel =
            ViewModelProvider(
                this,
                JobViewModelFactory(
                    jobRepository,
                    userRepository,
                    categoryRepository,
                    skillRepository,
                    attachmentRepository
                )
            )[JobViewModel::class.java]

        val onAttachmentClickListener = { position: Int, item: AttachmentEntity ->

        }

        val onActionBtnClickListener = { position: Int, item: AttachmentEntity ->

        }

        attachmentAdapter =
            ViewAttachmentsAdapter(
                mutableListOf(),
                onAttachmentClickListener,
                onActionBtnClickListener
            )

        viewJobBinding.jobAttachments.adapter = attachmentAdapter
    }

    private fun getJobObserver() {
        jobViewModel.job.observe(viewLifecycleOwner) {

            if (it.attachments.isEmpty()) {
                viewJobBinding.noAttachment.visibility = View.VISIBLE
                viewJobBinding.jobAttachments.visibility = View.GONE
            } else {
                viewJobBinding.noAttachment.visibility = View.GONE
                viewJobBinding.jobAttachments.visibility = View.VISIBLE
            }

            viewJobBinding.jobWithCategoriesAndSkillsAndAttachments = it
            viewJobBinding.executePendingBindings()
        }
    }

    private fun getJobLoadingObserver() {
        jobViewModel.loadingState.observe(viewLifecycleOwner) {
            viewJobBinding.progressBar.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}