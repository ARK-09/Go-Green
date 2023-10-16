package com.arkindustries.gogreen.ui.views

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.arkindustries.gogreen.AppContext
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.api.GeocodingClient
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.api.request.AttachmentRequest
import com.arkindustries.gogreen.api.request.UpdateUserRequest
import com.arkindustries.gogreen.api.response.GeocodingSearchResponse
import com.arkindustries.gogreen.api.response.Language
import com.arkindustries.gogreen.api.response.Location
import com.arkindustries.gogreen.api.response.Skill
import com.arkindustries.gogreen.api.services.FileService
import com.arkindustries.gogreen.api.services.GeocodingService
import com.arkindustries.gogreen.api.services.ProfileService
import com.arkindustries.gogreen.api.services.SkillService
import com.arkindustries.gogreen.database.AppDatabase
import com.arkindustries.gogreen.database.entites.AttachmentEntity
import com.arkindustries.gogreen.database.entites.SkillEntity
import com.arkindustries.gogreen.databinding.ActivityEditProfileBinding
import com.arkindustries.gogreen.ui.adapters.LabelledItemAdapter
import com.arkindustries.gogreen.ui.adapters.LanguageAdapter
import com.arkindustries.gogreen.ui.adapters.LocationAdapter
import com.arkindustries.gogreen.ui.models.LabelledItem
import com.arkindustries.gogreen.ui.repositories.FileRepository
import com.arkindustries.gogreen.ui.repositories.GeocodingRepository
import com.arkindustries.gogreen.ui.repositories.ProfileRepository
import com.arkindustries.gogreen.ui.repositories.SkillRepository
import com.arkindustries.gogreen.ui.repositories.UserRepository
import com.arkindustries.gogreen.ui.viewmodels.FileViewModel
import com.arkindustries.gogreen.ui.viewmodels.GeocodingViewModel
import com.arkindustries.gogreen.ui.viewmodels.ProfileViewModel
import com.arkindustries.gogreen.ui.viewmodels.SkillViewModel
import com.arkindustries.gogreen.ui.viewmodels.UserViewModel
import com.arkindustries.gogreen.ui.viewmodels.factory.FileViewModelFactory
import com.arkindustries.gogreen.ui.viewmodels.factory.GeocodingViewModelFactory
import com.arkindustries.gogreen.ui.viewmodels.factory.ProfileViewModelFactory
import com.arkindustries.gogreen.ui.viewmodels.factory.SkillViewModelFactory
import com.arkindustries.gogreen.ui.viewmodels.factory.UserViewModelFactory
import com.arkindustries.gogreen.utils.FilePickerUtil
import com.arkindustries.gogreen.utils.FileUtils
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import java.util.UUID

class EditProfile : AppCompatActivity(), AddLanguagesFragment.OnLanguageSelectListener,
    AddSkillsFragment.OnSkillsSelectListener {
    private lateinit var editProfileBinding: ActivityEditProfileBinding
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var filePickerUtil: FilePickerUtil
    private lateinit var fileService: FileService
    private lateinit var fileRepository: FileRepository
    private lateinit var fileViewModel: FileViewModel
    private lateinit var profileService: ProfileService
    private lateinit var profileRepository: ProfileRepository
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var appDatabase: AppDatabase
    private lateinit var skillService: SkillService
    private lateinit var skillRepository: SkillRepository
    private lateinit var skillsViewModel: SkillViewModel
    private lateinit var geocodingService: GeocodingService
    private lateinit var geocodingViewModel: GeocodingViewModel
    private lateinit var geocodingRepository: GeocodingRepository
    private lateinit var skillsAdapter: LabelledItemAdapter<SkillEntity>
    private lateinit var locationAdapter: LocationAdapter
    private lateinit var languagesAdapter: LanguageAdapter
    private var languages = mutableListOf<Language>()
    private var skills = mutableListOf<String>()
    private var userLocation: Location? = null
    private var userAddress: String? = null
    private lateinit var handler: Handler
    private lateinit var searchRunnable: Runnable
    private val debounceDelay: Long = 500
    private var userId: String? = null
    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 112233
    private var isPermissionGranted = false
    private var oldProfileImageUrl: String = ""
    private var userProfileImage: AttachmentEntity? = null
    private var isPasswordUpdated: Boolean = false
    private var isAboutUpdated: Boolean = false
    private var isUserClient: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editProfileBinding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(editProfileBinding.root)

        isUserClient = AppContext.getInstance().currentUser.userType == "client"

        userId = intent.getStringExtra("userId")

        if (userId.isNullOrEmpty()) {
            Snackbar.make(editProfileBinding.root, "Invalid user id", Snackbar.LENGTH_SHORT).show()
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
        profileService = RetrofitClient.createProfileService(this)
        profileRepository = ProfileRepository(profileService)
        fileService = RetrofitClient.createFileService(this)
        fileRepository = FileRepository(fileService, appDatabase.attachmentDao())
        skillService = RetrofitClient.createSkillService(this)
        skillRepository = SkillRepository(skillService, appDatabase.skillDao())
        geocodingService = GeocodingClient.createGeocodingService()
        geocodingRepository = GeocodingRepository(geocodingService)

        handler = Handler(Looper.getMainLooper())
        searchRunnable = Runnable { }

        val userService = RetrofitClient.createUserService(this)
        val userRepository = UserRepository(userService, appDatabase.userDao())

        fileViewModel =
            ViewModelProvider(this, FileViewModelFactory(fileRepository))[FileViewModel::class.java]

        geocodingViewModel = ViewModelProvider(
            this,
            GeocodingViewModelFactory(geocodingRepository)
        )[GeocodingViewModel::class.java]
        profileViewModel = ViewModelProvider(
            this,
            ProfileViewModelFactory(profileRepository)
        )[ProfileViewModel::class.java]
        userViewModel =
            ViewModelProvider(this, UserViewModelFactory(userRepository))[UserViewModel::class.java]

        if (!isUserClient) {
            skillsViewModel = ViewModelProvider(
                this,
                SkillViewModelFactory(skillRepository)
            )[SkillViewModel::class.java]

            val skillItemClickListener =
                object : LabelledItemAdapter.OnItemClickListener<SkillEntity> {
                    override fun onItemClick(item: LabelledItem<SkillEntity>) {
                    }

                }

            skillsAdapter = LabelledItemAdapter(listener = skillItemClickListener)
            editProfileBinding.skillsRv.adapter = skillsAdapter
        } else {
            editProfileBinding.skillsContainer.visibility = View.INVISIBLE
        }

        locationAdapter = LocationAdapter(listener = object : LocationAdapter.OnItemClickListener {
            override fun onItemClick(item: GeocodingSearchResponse) {
                editProfileBinding.searchView.setQuery(item.display_name, false)
                editProfileBinding.locationRv.visibility = View.GONE
                userLocation = Location("Point", mutableListOf(item.lon, item.lat))
                userAddress = item.display_name
            }
        })

        languagesAdapter = LanguageAdapter { }

        editProfileBinding.languageRv.adapter = languagesAdapter
        editProfileBinding.locationRv.adapter = locationAdapter
        editProfileBinding.editImage.setOnClickListener {
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
                isPasswordUpdated = editProfileBinding.currentPasswordTi.editText?.length()!! >= 8
            }

        }
        editProfileBinding.currentPasswordTi.editText?.addTextChangedListener(currentPasswordChangedListener)

        val aboutChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                isPasswordUpdated = editProfileBinding.currentPasswordTi.editText?.length()!! >= 8
            }

        }
        editProfileBinding.aboutTi.editText?.addTextChangedListener(aboutChangedListener)

        editProfileBinding.searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
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

        editProfileBinding.addLanguage.setOnClickListener {
            showAddLanguageDialog()
        }

        editProfileBinding.addSkills.setOnClickListener {
            showAddSkillsDialog()
        }

        editProfileBinding.updateProfile.setOnClickListener {
            if (validateProfileData()) {
                val fullName = editProfileBinding.nameTi.editText?.text.toString()
                val about = editProfileBinding.aboutTi.editText?.text.toString()
                val email = editProfileBinding.emailTi.editText?.text.toString()
                val password = editProfileBinding.passwordTi.editText?.text.toString()
                val currentPassword = editProfileBinding.currentPasswordTi.editText?.text.toString()
                val phoneNo = editProfileBinding.phoneNoTi.editText?.text.toString()

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

        editProfileBinding.backBtn.setOnClickListener {
            finish()
        }

        profileViewModel.getUserProfile(userId!!)

        geocodingSearchObserver()
        geocodingReverseObserver()
        getUserProfileObserver()
        getUserProfileErrorObserver()
        updateUserObserver ()
        updateUserProfileLoadingObserver()
        updateUserProfileObserver()
        updateUserProfileErrorObserver()
        getFileObserver()
        getFileErrorObserver()
        uploadErrorObserver()
        uploadProgressObserver()
        uploadResultObserver()
    }

    override fun onSkillsSelect(skills: List<Skill>) {
        skills.forEach {
            this@EditProfile.skills.add(it._id)
        }
        val skillEntities = skills.map { skill: Skill ->
            return@map LabelledItem<SkillEntity>(skill._id, skill.title)
        }
        skillsAdapter.appendList(skillEntities)
        editProfileBinding.noSkillsTv.visibility = View.INVISIBLE
        editProfileBinding.skillsRv.visibility = View.VISIBLE
    }

    override fun onLanguageSelect(language: Language) {
        languages.add(language)
        languagesAdapter.appendList(mutableListOf(language))
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
                editProfileBinding.fileProgress.visibility = View.VISIBLE
                Glide.with(this).load(file).error(R.drawable.ic_default_user)
                    .into(editProfileBinding.userImage)
                fileViewModel.uploadFiles(mutableListOf(Pair(UUID.randomUUID().toString(), file)))
            }
        }
    }

    private fun showAddLanguageDialog() {
        val addLanguagesFragment = AddLanguagesFragment()
        addLanguagesFragment.show(supportFragmentManager, "add_languages_dialog")
    }

    private fun showAddSkillsDialog() {
        val addSkillsFragment = AddSkillsFragment()
        addSkillsFragment.show(supportFragmentManager, "add_skills_dialog")
    }

    private fun geocodingSearchObserver() {
        geocodingViewModel.geocodingSearch.observe(this) {
            val locations = it.map { location ->
                return@map location
            }

            editProfileBinding.locationRv.visibility = View.VISIBLE

            locationAdapter.updateData(locations)
        }
    }

    private fun geocodingReverseObserver() {
        geocodingViewModel.geocodingReverse.observe(this) {
            val locationResult = if (((it.address.city ?: it.address.state) + ", " + it.address.country).replace(", ", "").isEmpty()) {
                ""
            } else {
                (it.address.city ?: it.address.state) + ", " + it.address.country
            }

            editProfileBinding.searchView.setQuery(locationResult, false)
        }
    }

    private fun uploadResultObserver() {
        fileViewModel.uploadResult.observe(this) {
            userProfileImage = it[0]
            oldProfileImageUrl = userProfileImage?.url.toString()
            editProfileBinding.fileProgress.visibility = View.GONE

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
            editProfileBinding.fileProgress.progress = it.second
        }
    }

    private fun uploadErrorObserver() {
        fileViewModel.uploadError.observe(this) {
            Snackbar.make(editProfileBinding.root, it.message!!, Snackbar.LENGTH_SHORT).show()
            Glide.with(this).load(R.drawable.ic_default_user).into(editProfileBinding.userImage)
        }
    }

    private fun getUserProfileObserver() {
        profileViewModel.userProfileResponse.observe(this) {
            editProfileBinding.progressBar.visibility = View.GONE

            if (!it.profile.location.coordinates.isNullOrEmpty()) {
                geocodingViewModel.reverseGeocode(
                    it.profile.location.coordinates[1],
                    it.profile.location.coordinates[0]
                )
            }

            editProfileBinding.userNameTv.text = it.profile.user.name
            editProfileBinding.nameTi.editText?.setText(it.profile.user.name)
            editProfileBinding.emailTi.editText?.setText(it.profile.user.email)
            editProfileBinding.aboutTi.editText?.setText(it.profile.about.ifEmpty { " " })
            editProfileBinding.phoneNoTi.editText?.setText(it.profile.user.phoneNo)
            languagesAdapter.submitList(it.profile.languages)

            val labelledItem = it.profile.skills.map { skill ->
                return@map LabelledItem<SkillEntity>(skill._id, skill.title)
            }

            if (labelledItem.isNotEmpty()) {
                skillsAdapter.updateData(labelledItem)
                editProfileBinding.noSkillsTv.visibility = View.INVISIBLE
            } else {
                editProfileBinding.noSkillsTv.visibility = View.VISIBLE
            }

            if (it.profile.user.image.id.isNotEmpty()) {
                fileViewModel.getFile(it.profile.user.image.id)
            }
        }
    }

    private fun getUserProfileErrorObserver() {
        profileViewModel.errorUserProfile.observe(this) {
            Snackbar.make(editProfileBinding.root, it.message!!, Snackbar.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateUserObserver () {

    }

    private fun updateUserProfileLoadingObserver() {
        profileViewModel.loadingUpdateUserProfile.observe(this) {
            editProfileBinding.progressBar.visibility = if (it) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
        }
    }

    private fun updateUserProfileObserver() {
        profileViewModel.loadingUpdateUserProfile.observe(this) {
            Snackbar.make(
                editProfileBinding.root,
                "Profile Updated successfully",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateUserProfileErrorObserver() {
        profileViewModel.errorUpdateUserProfile.observe(this) {
            Snackbar.make(editProfileBinding.root, it.message!!, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun getFileObserver() {
        fileViewModel.getFileResult.observe(this) {
            if (it.mimeType.contains("image")) {
                oldProfileImageUrl = it.url.toString()
                Glide.with(this).load(it.url).error(R.drawable.ic_default_user)
                    .into(editProfileBinding.userImage)
            }
        }
    }

    private fun getFileErrorObserver() {
        fileViewModel.getFileError.observe(this) {
            Snackbar.make(
                editProfileBinding.root,
                "Error getting user profile: " + it.message!!,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun validateProfileData(): Boolean {
        val fullName = editProfileBinding.nameTi.editText?.text.toString()
        val about = editProfileBinding.aboutTi.editText?.text.toString()
        val email = editProfileBinding.emailTi.editText?.text.toString()
        val password = editProfileBinding.passwordTi.editText?.text.toString()
        val currentPassword = editProfileBinding.currentPasswordTi.editText?.text.toString()
        val phoneNo = editProfileBinding.phoneNoTi.editText?.text.toString()

        if (fullName.isEmpty()) {
            editProfileBinding.nameTi.error = "Full name is a required field."
            return false
        }

        if (about.isEmpty() && isAboutUpdated) {
            editProfileBinding.aboutTi.error = "About is a required field."
            return false
        }

        if (email.isEmpty()) {
            editProfileBinding.emailTi.error = "Email is required required field."
            return false
        }

        if (isPasswordUpdated) {
            if (password.isEmpty()) {
                editProfileBinding.passwordTi.error = "Password is required field."
                return false
            }

            if (password.compareTo(currentPassword) == 0) {
                editProfileBinding.currentPasswordTi.error =
                    "New Password and current password should not be same."
                return false
            }
        }

        if (phoneNo.isEmpty()) {
            editProfileBinding.phoneNoTi.error = "Phone no is required field."
            return false
        }

        return true
    }
}