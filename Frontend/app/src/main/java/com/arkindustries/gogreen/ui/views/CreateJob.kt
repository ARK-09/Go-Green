package com.arkindustries.gogreen.ui.views

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.transition.TransitionInflater
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.api.GeocodingClient
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.api.request.AttachmentRequest
import com.arkindustries.gogreen.api.request.CreateJobRequest
import com.arkindustries.gogreen.api.request.LocationRequest
import com.arkindustries.gogreen.api.response.GeocodingSearchResponse
import com.arkindustries.gogreen.api.services.CategoryService
import com.arkindustries.gogreen.api.services.FileService
import com.arkindustries.gogreen.api.services.GeocodingService
import com.arkindustries.gogreen.api.services.JobService
import com.arkindustries.gogreen.api.services.SkillService
import com.arkindustries.gogreen.api.services.UserService
import com.arkindustries.gogreen.database.AppDatabase
import com.arkindustries.gogreen.database.entites.AttachmentEntity
import com.arkindustries.gogreen.database.entites.CategoryEntity
import com.arkindustries.gogreen.database.entites.SkillEntity
import com.arkindustries.gogreen.databinding.ActivityCreateJobBinding
import com.arkindustries.gogreen.ui.adapters.LabelledItemAdapter
import com.arkindustries.gogreen.ui.adapters.LocationAdapter
import com.arkindustries.gogreen.ui.adapters.UploadAttachmentsAdapter
import com.arkindustries.gogreen.ui.models.LabelledItem
import com.arkindustries.gogreen.ui.repositories.CategoryRepository
import com.arkindustries.gogreen.ui.repositories.FileRepository
import com.arkindustries.gogreen.ui.repositories.GeocodingRepository
import com.arkindustries.gogreen.ui.repositories.JobRepository
import com.arkindustries.gogreen.ui.repositories.SkillRepository
import com.arkindustries.gogreen.ui.repositories.UserRepository
import com.arkindustries.gogreen.ui.viewmodels.CategoryViewModel
import com.arkindustries.gogreen.ui.viewmodels.FileViewModel
import com.arkindustries.gogreen.ui.viewmodels.GeocodingViewModel
import com.arkindustries.gogreen.ui.viewmodels.JobViewModel
import com.arkindustries.gogreen.ui.viewmodels.SkillViewModel
import com.arkindustries.gogreen.ui.viewmodels.factory.CategoryViewModelFactory
import com.arkindustries.gogreen.ui.viewmodels.factory.GeocodingViewModelFactory
import com.arkindustries.gogreen.ui.viewmodels.factory.JobViewModelFactory
import com.arkindustries.gogreen.ui.viewmodels.factory.SkillViewModelFactory
import com.arkindustries.gogreen.utils.FilePickerUtil
import com.arkindustries.gogreen.utils.FileUtils
import java.io.File
import java.util.UUID

class CreateJob : Fragment() {
    private lateinit var createJobBinding: ActivityCreateJobBinding
    private lateinit var jobViewModel: JobViewModel
    private lateinit var categoriesViewModel: CategoryViewModel
    private lateinit var skillsViewModel: SkillViewModel
    private lateinit var locationAdapter: LocationAdapter
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var filePickerUtil: FilePickerUtil
    private lateinit var fileService: FileService
    private lateinit var fileRepository: FileRepository
    private lateinit var fileViewModel: FileViewModel
    private lateinit var attachmentAdapter: UploadAttachmentsAdapter
    private lateinit var database: AppDatabase
    private lateinit var jobService: JobService
    private lateinit var userService: UserService
    private lateinit var categoryService: CategoryService
    private lateinit var skillService: SkillService
    private lateinit var jobRepository: JobRepository
    private lateinit var userRepository: UserRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var skillRepository: SkillRepository
    private lateinit var geocodingService: GeocodingService
    private lateinit var geocodingViewModel: GeocodingViewModel
    private lateinit var geocodingRepository: GeocodingRepository
    private lateinit var categoriesAdapter: LabelledItemAdapter<CategoryEntity>
    private lateinit var skillsAdapter: LabelledItemAdapter<SkillEntity>
    private var jobLocation: LocationRequest? = null
    private var jobDuration = "Less than 1 month"
    private var jobCategories = mutableListOf<String>()
    private var jobSkills = mutableListOf<String>()
    private var jobType = "fixed"
    private lateinit var handler: Handler
    private lateinit var searchRunnable: Runnable
    private val debounceDelay: Long = 500
    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 123
    private val MAX_FILE = 5
    private var isPermissionGranted = false
    private val recentlyPickedFiles = mutableListOf<AttachmentEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val allowedFileTypes = arrayOf(
            "image/*",
            "video/*"
        )

        filePickerUtil = FilePickerUtil(requireContext(), allowedFileTypes)

        filePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(), filePickerUtil::handleFilePickerResult
        )

        database = AppDatabase.getInstance(requireContext())
        jobService = RetrofitClient.createJobService(requireContext())
        userService = RetrofitClient.createUserService(requireContext())
        categoryService = RetrofitClient.createCategoryService(requireContext())
        skillService = RetrofitClient.createSkillService(requireContext())
        fileService = RetrofitClient.createFileService(requireContext())
        fileRepository = FileRepository(fileService, database.attachmentDao())
        fileViewModel = FileViewModel(fileRepository)

        jobRepository = JobRepository(database.jobDao(), jobService)
        userRepository = UserRepository(userService, database.userDao())
        categoryRepository = CategoryRepository(categoryService, database.categoryDao())
        skillRepository = SkillRepository(skillService, database.skillDao())
        attachmentAdapter = UploadAttachmentsAdapter(progress = 0) { _, item ->
            fileViewModel.deleteFile(item.attachmentId)
        }

        geocodingService = GeocodingClient.createGeocodingService()
        geocodingRepository = GeocodingRepository(geocodingService)
        geocodingViewModel = ViewModelProvider(
            this,
            GeocodingViewModelFactory(geocodingRepository)
        )[GeocodingViewModel::class.java]

        handler = Handler(Looper.getMainLooper())
        searchRunnable = Runnable { }

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

        categoriesViewModel =
            ViewModelProvider(
                this,
                CategoryViewModelFactory(categoryRepository)
            )[CategoryViewModel::class.java]
        skillsViewModel =
            ViewModelProvider(
                this,
                SkillViewModelFactory(skillRepository)
            )[SkillViewModel::class.java]

        val categoryItemClickListener =
            object : LabelledItemAdapter.OnItemClickListener<CategoryEntity> {
                override fun onItemClick(item: LabelledItem<CategoryEntity>) {
                    jobCategories.add(item.id)
                }

            }

        val skillItemClickListener = object : LabelledItemAdapter.OnItemClickListener<SkillEntity> {
            override fun onItemClick(item: LabelledItem<SkillEntity>) {
                jobSkills.add(item.id)
            }

        }

        categoriesAdapter = LabelledItemAdapter(listener = categoryItemClickListener)
        skillsAdapter = LabelledItemAdapter(listener = skillItemClickListener)
        locationAdapter = LocationAdapter(listener = object : LocationAdapter.OnItemClickListener {
            override fun onItemClick(item: GeocodingSearchResponse) {
                createJobBinding.searchView.setQuery(item.display_name, false)
                createJobBinding.searchRv.visibility = View.GONE
                jobLocation = LocationRequest(coordinates = mutableListOf(item.lon.toFloat(), item.lat.toFloat()))
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        createJobBinding = ActivityCreateJobBinding.inflate(inflater, container, false)
        enterTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.slide_in_bottom)
        exitTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.slide_out_bottom)
        return createJobBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createJobBinding.attachmentRv.adapter = attachmentAdapter
        createJobBinding.attachmentRv.isNestedScrollingEnabled = true

        createJobBinding.deadlineSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val duration = parent?.adapter?.getItem(position).toString()
                jobDuration = duration.ifEmpty {
                    jobDuration
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        createJobBinding.categoryRv.adapter = categoriesAdapter
        createJobBinding.skillsRv.adapter = skillsAdapter
        createJobBinding.searchRv.adapter = locationAdapter

        createJobBinding.addAttachment.setOnClickListener {
            if (isPermissionGranted) {
                filePickerUtil.pickFilesUsingLauncher(filePickerLauncher)
            } else {
                requestPermission()
                filePickerUtil.pickFilesUsingLauncher(filePickerLauncher)
            }

            handelFilePickerResults()
        }

        createJobBinding.createJob.setOnClickListener {
            if (isValidJobData()) {
                val attachments = attachmentAdapter.dataList.map { attachmentEntity ->
                    return@map AttachmentRequest(
                        attachmentEntity.attachmentId,
                        attachmentEntity.mimeType,
                        attachmentEntity.originalName,
                        attachmentEntity.createdDate!!
                    )
                }

                val jobRequest = CreateJobRequest(
                    createJobBinding.jobTitleTi.editText?.text.toString(),
                    createJobBinding.jobDescriptionTi.editText?.text.toString(),
                    jobCategories, jobSkills,
                    createJobBinding.jobBudgetTi.editText?.text.toString().toDouble(),
                    jobDuration, jobType,
                    null,
                    jobLocation!!
                )

                if (attachments.isNotEmpty()) {
                    jobRequest.attachments = attachments
                }
                jobViewModel.createJob(jobRequest)
                createJobObserver()
                createJobErrorObserver()
            }
        }

        createJobBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    handler.removeCallbacks(searchRunnable)

                    searchRunnable = Runnable {
                        val searchText = query.toString()
                        geocodingViewModel.searchAddress(searchText)
                    }
                    handler.postDelayed(searchRunnable, debounceDelay)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })

        geocodingViewModel.geocodingSearch.observe(viewLifecycleOwner) {
            val locations = it.map { location ->
                return@map location
            }

            createJobBinding.searchRv.visibility = View.VISIBLE

            locationAdapter.updateData(locations)
        }

        categoriesResultObserver()
        skillsResultObserver()
        loadingObserver()
        uploadProgressObserver()
        attachmentErrorObserver()
        uploadResultsObserver()
        deleteFileObserver()
        deleteFileErrorObserver()
    }

    private fun categoriesResultObserver() {
        categoriesViewModel.categories.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                val categories = it.map { categoryEntity ->
                    return@map LabelledItem<CategoryEntity>(
                        categoryEntity.categoryId,
                        categoryEntity.title
                    )
                }
                categoriesAdapter.updateData(categories)
            }
        }
    }

    private fun skillsResultObserver() {
        skillsViewModel.skills.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                val skills = it.map { skillEntity ->
                    return@map LabelledItem<SkillEntity>(skillEntity.skillId, skillEntity.title)
                }
                skillsAdapter.updateData(skills)
            }
        }
    }

    private fun isValidJobData(): Boolean {
        if (createJobBinding.jobTitleTi.editText?.text.isNullOrEmpty()) {
            createJobBinding.jobTitleTi.error = "Job title is required"
            return false
        }

        if (createJobBinding.jobDescriptionTi.editText?.text.isNullOrEmpty()) {
            createJobBinding.jobDescriptionTi.error = "Job description is required"
            return false
        }

        if (jobLocation == null) {
            Toast.makeText(requireContext(), "Please provide job location", Toast.LENGTH_SHORT).show()
            return false
        }

        if (jobCategories.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one category", Toast.LENGTH_SHORT).show()

            return false
        }

        if (jobSkills.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one skill", Toast.LENGTH_SHORT).show()
            return false
        }

        if (createJobBinding.jobBudgetTi.editText?.text.isNullOrEmpty()) {
            createJobBinding.jobBudgetTi.error = "Please provide valid job budget."
            return false
        }

        return true
    }

    private fun createJobObserver() {
        jobViewModel.job.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "Job created successfully", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadingObserver() {
        jobViewModel.loadingState.observe(viewLifecycleOwner) {
            createJobBinding.progress.visibility = if (it) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
        }
    }

    private fun createJobErrorObserver() {
        jobViewModel.error.observe(viewLifecycleOwner) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage(it.message)
                .setTitle("Error while creating job")
                .setPositiveButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                }
            builder.show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        isPermissionGranted = if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
    }

    private fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            isPermissionGranted = true
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("To add attachments, please allow access to your device's storage.")
                .setTitle("Permission Required")
                .setCancelable(false)
                .setPositiveButton("Ok") { dialog, _ ->
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        READ_EXTERNAL_STORAGE_REQUEST_CODE
                    )
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    isPermissionGranted = false
                    dialog.dismiss()
                }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_REQUEST_CODE
            )
        }
    }

    private fun attachmentErrorObserver() {
        fileViewModel.uploadError.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it.message!!, Toast.LENGTH_SHORT).show()
            recentlyPickedFiles.forEach { recentlyPickedFile ->
                val indexOfCurrentFile = attachmentAdapter.dataList.indexOf(recentlyPickedFile)
                attachmentAdapter.dataList.removeAt(indexOfCurrentFile)
                attachmentAdapter.notifyItemRemoved(indexOfCurrentFile)
            }
        }
    }

    private fun handelFilePickerResults() {
        filePickerUtil.getFiles { selectedFiles ->

            if (selectedFiles.size > (MAX_FILE - attachmentAdapter.dataList.size)) {
                Toast.makeText(requireContext(), "You can upload up to five files. The remaining limit is: " + (MAX_FILE - attachmentAdapter.dataList.size), Toast.LENGTH_SHORT).show()
                return@getFiles
            }

            recentlyPickedFiles.clear()

            val attachments = getAttachments(selectedFiles)
            val files = getFiles(attachments)

            if (attachments.isNotEmpty() && files.isNotEmpty()) {
                recentlyPickedFiles.addAll(attachments)
                attachmentAdapter.addAllItems(attachments)
                fileViewModel.uploadFiles(files)
            }
        }
    }

    private fun uploadProgressObserver() {
        fileViewModel.uploadProgressMap.observe(viewLifecycleOwner) {
            val fileIndex = attachmentAdapter.dataList.indexOfFirst { attachmentEntity ->
                return@indexOfFirst attachmentEntity.attachmentId === it.first
            }

            val attachmentItem =
                createJobBinding.attachmentRv.findViewHolderForAdapterPosition(fileIndex)?.itemView?.findViewById<ProgressBar>(
                    R.id.file_progress
                )
            if (attachmentItem != null) {
                attachmentItem.visibility = View.VISIBLE
            }
            attachmentItem?.progress = it.second

        }
    }

    private fun getAttachments(files: List<Uri>): List<AttachmentEntity> {
        val attachments = mutableListOf<AttachmentEntity>()

        files.forEach { fileUri ->
            val fileMimeType = filePickerUtil.getFileMimeType(fileUri)
            val fileName = filePickerUtil.getOriginalFileName(fileUri)
            if (fileMimeType != null && fileName != null) {
                attachments.add(
                    AttachmentEntity(
                        UUID.randomUUID().toString(),
                        fileMimeType,
                        fileName,
                        null,
                        fileUri.toString()
                    )
                )
            }
        }

        return attachments
    }

    private fun getFiles(attachments: List<AttachmentEntity>): List<Pair<String, File>> {
        val result = mutableListOf<Pair<String, File>>()

        attachments.forEach { attachment ->
            val file = FileUtils.getFileFromUri(requireContext(), attachment.url!!.toUri())
            if (file != null) {
                result.add(Pair(attachment.attachmentId, file))
            }
        }

        return result
    }

    private fun uploadResultsObserver() {
        fileViewModel.uploadResult.observe(viewLifecycleOwner) {
            attachmentAdapter.removeItem { attachmentEntity ->
                attachmentEntity.url != null
            }

            attachmentAdapter.addAllItems(it)
        }
    }

    private fun deleteFileObserver() {
        fileViewModel.deleteFileResult.observe(viewLifecycleOwner) {
            attachmentAdapter.removeItem { attachmentEntity ->
                attachmentEntity.attachmentId === it
            }
        }
    }

    private fun deleteFileErrorObserver() {
        fileViewModel.deleteFileError.observe(viewLifecycleOwner) {
            attachmentAdapter.removeItem { attachmentEntity ->
                attachmentEntity.attachmentId === it.data
            }
            Toast.makeText(requireContext(), "Error deleting attachment: " + it.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(searchRunnable)
    }
}