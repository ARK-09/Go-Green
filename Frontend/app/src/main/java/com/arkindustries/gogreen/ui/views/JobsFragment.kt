package com.arkindustries.gogreen.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.transition.TransitionInflater
import com.arkindustries.gogreen.AppContext
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.api.services.CategoryService
import com.arkindustries.gogreen.api.services.FileService
import com.arkindustries.gogreen.api.services.JobService
import com.arkindustries.gogreen.api.services.SkillService
import com.arkindustries.gogreen.api.services.UserService
import com.arkindustries.gogreen.database.AppDatabase
import com.arkindustries.gogreen.databinding.FragmentJobsBinding
import com.arkindustries.gogreen.ui.adapters.JobAdapter
import com.arkindustries.gogreen.ui.repositories.CategoryRepository
import com.arkindustries.gogreen.ui.repositories.FileRepository
import com.arkindustries.gogreen.ui.repositories.JobRepository
import com.arkindustries.gogreen.ui.repositories.SkillRepository
import com.arkindustries.gogreen.ui.repositories.UserRepository
import com.arkindustries.gogreen.ui.viewmodels.JobViewModel
import com.arkindustries.gogreen.ui.viewmodels.factory.JobViewModelFactory
import com.bumptech.glide.Glide

class JobsFragment : Fragment() {
    private lateinit var jobFragmentBinding: FragmentJobsBinding
    private lateinit var jobAdapter: JobAdapter
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
    private lateinit var fileRepository: FileRepository
    private var isUserClient: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isUserClient = AppContext.getInstance().currentUser.userType == "client"

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
        fileRepository = FileRepository(fileService, database.attachmentDao())

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

        jobAdapter = JobAdapter(
            onItemClick = { job ->
                val bundle = bundleOf("jobId" to job.jobId)
                parentFragmentManager.setFragmentResult("viewJob", bundle)
                jobFragmentBinding.root.findNavController()
                    .navigate(R.id.action_jobsFragment_to_viewJob)
            },
            onOfferClick = { job ->
                val bundle = bundleOf("jobId" to job.jobId)
                parentFragmentManager.setFragmentResult("createProposal", bundle)
                jobFragmentBinding.root.findNavController()
                    .navigate(R.id.action_jobsFragment_to_proposal)
            },
            // if user is client hide the send offer button
            isUserClient
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        jobFragmentBinding = FragmentJobsBinding.inflate(inflater)
        enterTransition =
            TransitionInflater.from(requireContext()).inflateTransition(R.transition.slide_out)
        exitTransition =
            TransitionInflater.from(requireContext()).inflateTransition(R.transition.slide_in)
        return jobFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (isUserClient) {
            jobFragmentBinding.searchEt.isEnabled = false
            jobFragmentBinding.searchBtn.isEnabled = false
        }

        jobFragmentBinding.recyclerView.adapter = jobAdapter

        jobFragmentBinding.searchEt.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                jobFragmentBinding.searchEt.helperText = ""
            } else {
                jobFragmentBinding.searchEt.helperText = "Search..."
            }
        }

        jobFragmentBinding.searchBtn.setOnClickListener {
            jobViewModel.searchJobs(
                jobFragmentBinding.searchEt.editText?.text.toString(),
                null,
                null,
                null,
                null,
                null
            )
        }

        jobFragmentBinding.swipeRefreshLayout.setOnRefreshListener {
            jobViewModel.searchJobs(null, null, null, null, null, null)
        }

        loadingStateObserver()
        resultsErrorObserver()

        if (isUserClient) {
            val userId = AppContext.getInstance().currentUser.userId
            jobViewModel.getJobs(userId)
            currentUserJobsObserver()
        } else {
            jobViewModel.searchJobs(null, null, null, null, null, null)
            searchJobsObserver()
        }
    }

    private fun searchJobsObserver() {
        jobViewModel.searchJobsResult.observe(viewLifecycleOwner) { jobs ->
            jobAdapter.submitList(jobs)

            if (jobs.isEmpty()) {
                displayError(true)
            } else {
                displayError(false)
            }
        }
    }

    private fun currentUserJobsObserver() {
        jobViewModel.jobs.observe(viewLifecycleOwner) { jobs ->
            jobAdapter.submitList(jobs)

            if (jobs.isEmpty()) {
                displayError(true)
            } else {
                displayError(false)
            }
        }
    }

    private fun loadingStateObserver() {
        jobViewModel.loadingState.observe(viewLifecycleOwner) { loading ->
            jobFragmentBinding.searchJobProgress.visibility = if (loading) {
                View.VISIBLE
            } else {
                View.GONE
            }

            if (!loading) {
                jobFragmentBinding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun resultsErrorObserver() {
        jobViewModel.error.observe(viewLifecycleOwner) { err ->
            if (err.status == "fail" || err.status == "error") {
                displayError(true)
            } else {
                displayError(false)
            }
        }
    }

    private fun displayError(visible: Boolean) {
        if (visible) {
            Glide.with(jobFragmentBinding.notFound)
                .load(R.drawable.not_found)
                .into(jobFragmentBinding.notFound)
            jobFragmentBinding.notFound
            jobFragmentBinding.notFound.visibility = View.VISIBLE
        } else {
            jobFragmentBinding.notFound.visibility = View.GONE
        }
    }
}