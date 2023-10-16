package com.arkindustries.gogreen.ui.views

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.arkindustries.gogreen.AppContext
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.api.request.AttachmentRequest
import com.arkindustries.gogreen.api.request.UpdateUserRequest
import com.arkindustries.gogreen.api.services.FileService
import com.arkindustries.gogreen.database.AppDatabase
import com.arkindustries.gogreen.database.entites.AttachmentEntity
import com.arkindustries.gogreen.databinding.ActivityAccountBinding
import com.arkindustries.gogreen.ui.repositories.FileRepository
import com.arkindustries.gogreen.ui.repositories.UserRepository
import com.arkindustries.gogreen.ui.viewmodels.FileViewModel
import com.arkindustries.gogreen.ui.viewmodels.UserViewModel
import com.arkindustries.gogreen.ui.viewmodels.factory.FileViewModelFactory
import com.arkindustries.gogreen.ui.viewmodels.factory.UserViewModelFactory
import com.arkindustries.gogreen.utils.FilePickerUtil
import com.arkindustries.gogreen.utils.FileUtils
import com.arkindustries.gogreen.utils.UserSessionManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import java.util.UUID

class Account : AppCompatActivity() {
    private lateinit var accountBinding: ActivityAccountBinding
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var filePickerUtil: FilePickerUtil
    private lateinit var fileService: FileService
    private lateinit var fileRepository: FileRepository
    private lateinit var fileViewModel: FileViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var appDatabase: AppDatabase
    private var userId: String? = null
    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 112233
    private var isPermissionGranted = false
    private var oldProfileImageUrl: String = ""
    private var userProfileImage: AttachmentEntity? = null
    private var isPasswordUpdated: Boolean = false
    private var isUserClient: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountBinding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(accountBinding.root)

        isUserClient = AppContext.getInstance().currentUser.userType == "client"

        userId = intent.getStringExtra("userId")

        if (userId.isNullOrEmpty()) {
            Snackbar.make(accountBinding.root, "Invalid user id", Snackbar.LENGTH_SHORT).show()
            finish()
        }

        val allowedFileTypes = arrayOf(
            "image/*",
        )

        filePickerUtil = FilePickerUtil(this, allowedFileTypes, false)

        filePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(), filePickerUtil::handleFilePickerResult
        )

        appDatabase = AppDatabase.getInstance(this)
        fileService = RetrofitClient.createFileService(this)
        fileRepository = FileRepository(fileService, appDatabase.attachmentDao())

        val userService = RetrofitClient.createUserService(this)
        val userRepository = UserRepository(userService, appDatabase.userDao())

        fileViewModel =
            ViewModelProvider(this, FileViewModelFactory(fileRepository))[FileViewModel::class.java]

        userViewModel =
            ViewModelProvider(this, UserViewModelFactory(userRepository))[UserViewModel::class.java]

        accountBinding.editImage.setOnClickListener {
            if (isPermissionGranted) {
                filePickerUtil.pickFilesUsingLauncher(filePickerLauncher)
            } else {
                requestPermission()
                filePickerUtil.pickFilesUsingLauncher(filePickerLauncher)
            }

            handelFilePickerResults()
        }

        val currentPasswordChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                isPasswordUpdated = accountBinding.currentPasswordTi.editText?.length()!! >= 8
            }

        }
        accountBinding.currentPasswordTi.editText?.addTextChangedListener(currentPasswordChangedListener)

        accountBinding.updateProfile.setOnClickListener {
            clearErrors()
            if (validateUserData()) {
                val fullName = accountBinding.nameTi.editText?.text.toString()
                val email = accountBinding.emailTi.editText?.text.toString()
                val password = accountBinding.passwordTi.editText?.text.toString()
                val currentPassword = accountBinding.currentPasswordTi.editText?.text.toString()
                val phoneNo = accountBinding.phoneNoTi.editText?.text.toString()

                userViewModel.updateUserById(
                    userId!!,
                    UpdateUserRequest(
                        fullName,
                        email,
                        if (isPasswordUpdated) password else null,
                        if (isPasswordUpdated) currentPassword else null,
                        phoneNo,
                        if (userProfileImage != null) {
                            AttachmentRequest(
                                userProfileImage!!.attachmentId,
                                userProfileImage!!.mimeType,
                                userProfileImage!!.mimeType,
                                userProfileImage!!.createdDate!!
                            )
                        } else null
                    )
                )
            }
        }

        accountBinding.backBtn.setOnClickListener {
            finish()
        }

        userViewModel.getUserById(userId!!)

        userObserver ()
        userLoadingObserver()
        userObserverError ()
        updateUserObserver ()
        loginUserObserver ()
        currentUserObserver ()
        updateUserErrorObserver ()
        getFileObserver()
        getFileErrorObserver()
        uploadErrorObserver()
        uploadProgressObserver()
        uploadResultObserver()
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
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            isPermissionGranted = true
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("To add attachments, please allow access to your device's storage.")
                .setTitle("Permission Required")
                .setCancelable(false)
                .setPositiveButton("Ok") { dialog, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        READ_EXTERNAL_STORAGE_REQUEST_CODE
                    )
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    isPermissionGranted = false
                    dialog.dismiss()
                }
            builder.show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_REQUEST_CODE
            )
        }
    }

    private fun handelFilePickerResults() {
        filePickerUtil.getFiles { selectedFiles ->

            val attachment = selectedFiles[0]
            val file = FileUtils.getFileFromUri(this, attachment)

            if (file != null) {
                accountBinding.fileProgress.visibility = View.VISIBLE
                Glide.with(this).load(file).error(R.drawable.ic_default_user)
                    .into(accountBinding.userImage)
                fileViewModel.uploadFiles(mutableListOf(Pair(UUID.randomUUID().toString(), file)))
            }
        }
    }

    private fun clearErrors () {
        accountBinding.nameTi.error = ""
        accountBinding.emailTi.error = ""
        accountBinding.currentPasswordTi.error = ""
        accountBinding.passwordTi.error = ""
        accountBinding.phoneNoTi.error = ""
        accountBinding.errorTv.visibility = View.INVISIBLE
    }

    private fun uploadResultObserver() {
        fileViewModel.uploadResult.observe(this) {
            userProfileImage = it[0]
            oldProfileImageUrl = userProfileImage?.url.toString()
            accountBinding.fileProgress.visibility = View.GONE

            if (userProfileImage != null) {
                userViewModel.updateUserById(
                    userId!!,
                    UpdateUserRequest(
                        null,
                        null,
                        null,
                        null,
                        null,
                        AttachmentRequest(
                            userProfileImage!!.attachmentId,
                            userProfileImage!!.mimeType,
                            userProfileImage!!.originalName,
                            userProfileImage!!.createdDate!!
                        )
                    )
                )
            }
        }
    }

    private fun uploadProgressObserver() {
        fileViewModel.uploadProgressMap.observe(this) {
            accountBinding.fileProgress.progress = it.second
        }
    }

    private fun uploadErrorObserver() {
        fileViewModel.uploadError.observe(this) {
            Snackbar.make(accountBinding.root, it.message!!, Snackbar.LENGTH_SHORT).show()
            Glide.with(this).load(R.drawable.ic_default_user).into(accountBinding.userImage)
        }
    }

    private fun userObserver () {
        userViewModel.userById.observe(this) {
            val user = it.data?.user
            if (user != null) {
                accountBinding.nameTi.editText?.setText(user.name)
                accountBinding.emailTi.editText?.setText(user.email)
                accountBinding.phoneNoTi.editText?.setText(user.phoneNo)
            }
        }
    }

    private fun userObserverError () {
        userViewModel.userByIdError.observe(this) {
            Snackbar.make(accountBinding.root, it.message!!, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun updateUserObserver () {
        userViewModel.updateUser.observe(this) {
            Snackbar.make(accountBinding.root, "Account updated successfully", Snackbar.LENGTH_SHORT).show()
            userViewModel.login(accountBinding.emailTi.editText?.text.toString(), accountBinding.passwordTi.editText?.text.toString())
        }
    }

    private fun loginUserObserver () {
        userViewModel.loginResult.observe(this) {
            if (it.data != null) {
                UserSessionManager.saveJwtToken(this, it.data.Jwt)
                userViewModel.currentUser()
            } else {
                Snackbar.make(
                    accountBinding.root,
                    "Failed to retrieve your session token. Please try again.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun currentUserObserver () {
        userViewModel.currentUserResult.observe(this) {
            if (it != null) {
                AppContext.initialize(it)
            } else {
                navigateToHome()
            }
        }
    }

    private fun updateUserErrorObserver () {
        userViewModel.updateUserError.observe(this) {
            accountBinding.errorTv.visibility = View.VISIBLE
            accountBinding.errorTv.text = it.message!!
        }
    }

    private fun userLoadingObserver () {
        userViewModel.loadingState.observe(this) {
            accountBinding.progressBar.visibility = if (it) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
        }
    }

    private fun getFileObserver() {
        fileViewModel.getFileResult.observe(this) {
            if (it.mimeType.contains("image")) {
                oldProfileImageUrl = it.url.toString()
                Glide.with(this).load(it.url).error(R.drawable.ic_default_user)
                    .into(accountBinding.userImage)
            }
        }
    }

    private fun getFileErrorObserver() {
        fileViewModel.getFileError.observe(this) {
            Snackbar.make(
                accountBinding.root,
                "Error getting user profile: " + it.message!!,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, Home::class.java)
        startActivity(intent)
        finish()
    }

    private fun validateUserData(): Boolean {
        val fullName = accountBinding.nameTi.editText?.text.toString()
        val email = accountBinding.emailTi.editText?.text.toString()
        val password = accountBinding.passwordTi.editText?.text.toString()
        val currentPassword = accountBinding.currentPasswordTi.editText?.text.toString()
        val phoneNo = accountBinding.phoneNoTi.editText?.text.toString()

        if (fullName.isEmpty()) {
            accountBinding.nameTi.error = "Full name is a required field."
            return false
        }

        if (email.isEmpty()) {
            accountBinding.emailTi.error = "Email is required required field."
            return false
        }

        if (isPasswordUpdated) {
            if (password.isEmpty()) {
                accountBinding.passwordTi.error = "Password is required field."
                return false
            }

            if (password.compareTo(currentPassword) == 0) {
                accountBinding.currentPasswordTi.error =
                    "New Password and current password should not be same."
                return false
            }
        }

        if (phoneNo.isEmpty()) {
            accountBinding.phoneNoTi.error = "Phone no is required field."
            return false
        }

        return true
    }
}