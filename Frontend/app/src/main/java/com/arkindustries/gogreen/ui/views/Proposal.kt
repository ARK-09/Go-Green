package com.arkindustries.gogreen.ui.views

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.api.request.AttachmentRequest
import com.arkindustries.gogreen.api.request.CreateJobProposalRequest
import com.arkindustries.gogreen.api.services.CategoryService
import com.arkindustries.gogreen.api.services.FileService
import com.arkindustries.gogreen.api.services.JobService
import com.arkindustries.gogreen.api.services.ProposalService
import com.arkindustries.gogreen.api.services.SkillService
import com.arkindustries.gogreen.api.services.UserService
import com.arkindustries.gogreen.database.AppDatabase
import com.arkindustries.gogreen.database.dao.AttachmentDao
import com.arkindustries.gogreen.database.dao.ProposalDao
import com.arkindustries.gogreen.database.entites.AttachmentEntity
import com.arkindustries.gogreen.database.entites.JobEntity
import com.arkindustries.gogreen.database.entites.UserEntity
import com.arkindustries.gogreen.databinding.ActivityProposalBinding
import com.arkindustries.gogreen.ui.adapters.UploadAttachmentsAdapter
import com.arkindustries.gogreen.ui.bindingadapters.location
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
import com.arkindustries.gogreen.utils.DateTimeUtils
import com.arkindustries.gogreen.utils.FilePickerUtil
import com.arkindustries.gogreen.utils.FileUtils
import java.io.File
import java.util.Locale
import java.util.UUID

class Proposal : Fragment() {
    private lateinit var proposalBinding: ActivityProposalBinding
    private lateinit var attachmentAdapter: UploadAttachmentsAdapter
    private lateinit var fileService: FileService
    private lateinit var attachmentDao: AttachmentDao
    private lateinit var fileRepository: FileRepository
    private lateinit var fileViewModel: FileViewModel
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var filePickerUtil: FilePickerUtil
    private lateinit var proposalService: ProposalService
    private lateinit var proposalDao: ProposalDao
    private lateinit var proposalRepository: ProposalRepository
    private lateinit var proposalViewModel: ProposalViewModel
    private lateinit var appDatabase: AppDatabase
    private lateinit var jobViewModel: JobViewModel
    private lateinit var jobService: JobService
    private lateinit var userService: UserService
    private lateinit var categoryService: CategoryService
    private lateinit var skillService: SkillService
    private lateinit var jobRepository: JobRepository
    private lateinit var userRepository: UserRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var skillRepository: SkillRepository
    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 123
    private val MAX_FILE = 5
    private var isPermissionGranted = false
    private val recentlyPickedFiles = mutableListOf<AttachmentEntity>()
    private var proposedDuration = "Less than 1 month"
    private var job: JobEntity? = null
    private lateinit var proposalId: String
    private var shouldCreateProposal: Boolean = true

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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        proposalBinding = ActivityProposalBinding.inflate(inflater)
        return proposalBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentFragmentManager.setFragmentResultListener("proposalForJobId", this) { key, bundle ->
            jobViewModel.getJobById(bundle.getString("jobId").toString())
            getJobObserver()
        }

        parentFragmentManager.setFragmentResultListener("proposalId", this) { key, bundle ->
            shouldCreateProposal = false
            proposalId = bundle.getString("proposalId").toString()
        }

        proposalBinding.root.getChildAt(0).setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val imm =
                        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                    v.performClick()
                }
            }
            true
        }

        if (!shouldCreateProposal) {
            proposalBinding.proposalActionBtn.text = "Send Offer"
        }

        proposalBinding.proposalActionBtn.setOnClickListener {
            if (shouldCreateProposal) {
                createProposal()
            } else {
                val bundle = bundleOf("proposalId" to proposalId)
                setFragmentResult("createRoom", bundle)
                proposalBinding.root.findNavController().navigate(R.id.action_proposal_to_rooms)
            }
        }

        appDatabase = AppDatabase.getInstance(requireContext())

        fileService = RetrofitClient.createFileService(requireContext())
        attachmentDao = appDatabase.attachmentDao()
        fileRepository = FileRepository(fileService, attachmentDao)
        fileViewModel = FileViewModel(fileRepository)

        proposalService = RetrofitClient.createProposalService(requireContext())
        proposalDao = appDatabase.proposalDao()
        proposalRepository = ProposalRepository(proposalDao, proposalService)
        proposalViewModel = ProposalViewModel(proposalRepository, fileRepository)
        jobService = RetrofitClient.createJobService(requireContext())
        jobRepository = JobRepository(appDatabase.jobDao(), jobService)
        userService = RetrofitClient.createUserService(requireContext())
        userRepository = UserRepository(userService, appDatabase.userDao())
        categoryService = RetrofitClient.createCategoryService(requireContext())
        categoryRepository = CategoryRepository(categoryService, appDatabase.categoryDao())
        skillService = RetrofitClient.createSkillService(requireContext())
        skillRepository = SkillRepository(skillService, appDatabase.skillDao())
        fileRepository = FileRepository(fileService, appDatabase.attachmentDao())

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

        val proposedDurationSpinnerValues = resources.getStringArray(R.array.project_timeline)
        val spinnerAdapter =
            ArrayAdapter(requireContext(), R.layout.spinner_dropdown, proposedDurationSpinnerValues)

        proposalBinding.proposedDuration.setText(proposedDurationSpinnerValues[0])
        proposalBinding.proposedDuration.setAdapter(spinnerAdapter)

        attachmentAdapter = UploadAttachmentsAdapter(progress = 0) { _, item ->
            fileViewModel.deleteFile(item.attachmentId)
        }

        proposalBinding.attachments.adapter = attachmentAdapter
        proposalBinding.attachments.isNestedScrollingEnabled = true

        proposalBinding.addAttachment.setOnClickListener {
            if (isPermissionGranted) {
                filePickerUtil.pickFilesUsingLauncher(filePickerLauncher)
            } else {
                requestPermission()
                filePickerUtil.pickFilesUsingLauncher(filePickerLauncher)
            }

            handelFilePickerResults()
        }

        proposalBinding.proposedDuration.onItemClickListener =
            OnItemClickListener { _, _, position, _ ->
                proposedDuration = proposedDurationSpinnerValues[position]
            }

        uploadProgressObserver()
        attachmentErrorObserver()
        uploadResultsObserver()
        deleteFileObserver()
        deleteFileErrorObserver()
        createProposalObserver()
    }

    private fun createProposalObserver() {
        proposalViewModel.createJobProposal.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "Proposal created successfully", Toast.LENGTH_SHORT)
                .show()
            parentFragmentManager.popBackStack(R.id.jobsFragment, 0)
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
                .setPositiveButton("Ok") { dialog, which ->
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

    private fun isValidateProposalData(): Boolean {
        if (proposalBinding.bidAmount.text.isNullOrEmpty()) {
            proposalBinding.bidAmount.error = "Please provide a bid amount."
            return false
        }

        if (proposalBinding.coverLetter.text.isNullOrBlank()) {
            proposalBinding.coverLetter.error = "Please provide cover letter"
            return false
        }

        return true
    }

    private fun createProposal() {
        if (job != null) {

            if (!isValidateProposalData()) {
                return
            }

            val attachments = attachmentAdapter.dataList.map { attachmentEntity ->
                return@map AttachmentRequest(
                    attachmentEntity.attachmentId,
                    attachmentEntity.mimeType,
                    attachmentEntity.originalName,
                    attachmentEntity.createdDate!!
                )
            }

            proposalViewModel.createJobProposal(
                job!!.jobId, CreateJobProposalRequest(
                    proposalBinding.bidAmount.text.toString().toDouble(),
                    proposalBinding.coverLetter.text.toString(),
                    proposedDuration,
                    if (attachments.isNotEmpty()) {
                        attachments
                    } else null
                )
            )
            createProposalErrorObserver()
        } else {
            Toast.makeText(
                requireContext(),
                "Please wait while we get the job",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun createProposalErrorObserver() {
        proposalViewModel.createJobProposalError.observe(viewLifecycleOwner) {
            if (it != null) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage(it.message)
                    .setTitle("Error while creating proposal")
                    .setPositiveButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }.show()
                parentFragmentManager.popBackStack(R.id.jobsFragment, 0)
            }
        }
    }

    private fun inflateJob() {
        if (job != null) {
            proposalBinding.jobTitleTv.text = job!!.title

            proposalBinding.jobDateTimeTv.text =
                DateTimeUtils.formatTimeAgo(job!!.createdDate, Locale.getDefault())
            proposalBinding.jobLocationTv.location(job!!.location)
        }
    }

    private fun getJobObserver() {
        jobViewModel.job.observe(viewLifecycleOwner) {
            job = JobEntity(
                it.job.jobId,
                it.job.title,
                it.job.description,
                it.job.budget,
                it.job.status,
                it.job.expectedDuration,
                it.job.paymentType,
                it.job.location,
                it.job.createdDate,
                user = UserEntity(
                    it.job.user!!.userId,
                    it.job.user.name,
                    it.job.user.email,
                    it.job.user.phoneNo,
                    it.job.user.userType,
                    it.job.user.image,
                    it.job.user.userStatus,
                    it.job.user.verified,
                    it.job.user.financeAllowed,
                    it.job.user.blocked,
                    it.job.user.blockedReason,
                    it.job.user.joinedDate
                )
            )
            inflateJob()
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
                Toast.makeText(
                    requireContext(),
                    "You can upload up to five files. The remaining limit is: " + (MAX_FILE - attachmentAdapter.dataList.size),
                    Toast.LENGTH_LONG
                ).show()

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
                proposalBinding.attachments.findViewHolderForAdapterPosition(fileIndex)?.itemView?.findViewById<ProgressBar>(
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
            Toast.makeText(
                requireContext(),
                "Error deleting attachment: " + it.message!!,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}