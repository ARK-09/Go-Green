package com.arkindustries.gogreen.ui.views

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import com.arkindustries.gogreen.AppContext
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.api.GeocodingClient
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.api.request.UpdateUserProfileRequest
import com.arkindustries.gogreen.api.response.GeocodingSearchResponse
import com.arkindustries.gogreen.api.response.Language
import com.arkindustries.gogreen.api.response.Location
import com.arkindustries.gogreen.api.response.Skill
import com.arkindustries.gogreen.api.services.FileService
import com.arkindustries.gogreen.api.services.GeocodingService
import com.arkindustries.gogreen.api.services.ProfileService
import com.arkindustries.gogreen.api.services.SkillService
import com.arkindustries.gogreen.database.AppDatabase
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
import com.arkindustries.gogreen.ui.viewmodels.FileViewModel
import com.arkindustries.gogreen.ui.viewmodels.GeocodingViewModel
import com.arkindustries.gogreen.ui.viewmodels.ProfileViewModel
import com.arkindustries.gogreen.ui.viewmodels.SkillViewModel
import com.arkindustries.gogreen.ui.viewmodels.factory.FileViewModelFactory
import com.arkindustries.gogreen.ui.viewmodels.factory.GeocodingViewModelFactory
import com.arkindustries.gogreen.ui.viewmodels.factory.ProfileViewModelFactory
import com.arkindustries.gogreen.ui.viewmodels.factory.SkillViewModelFactory
import com.arkindustries.gogreen.utils.DateTimeUtils
import com.arkindustries.gogreen.utils.FilePickerUtil
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar

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
    private val debounceDelay: Long = 2000
    private var userId: String? = null
    private var oldProfileImageUrl: String = ""
    private var isPasswordUpdated: Boolean = false
    private var isAboutUpdated: Boolean = false
    private var isUserClient: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
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

        val aboutChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                isPasswordUpdated = editProfileBinding.aboutTi.editText?.length()!! >= 1
            }

        }

        editProfileBinding.aboutTi.editText?.addTextChangedListener(aboutChangedListener)

        editProfileBinding.dob.editText?.setOnTouchListener { _: View, motion: MotionEvent ->
            if (motion.action == MotionEvent.ACTION_DOWN) {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendar.get(Calendar.MONTH)

                val datePickerDialog = DatePickerDialog(
                    this,
                    { _, year, month, day ->
                        editProfileBinding.dob.editText!!.setText("${day}/${(month + 1)}/${year}")
                    },
                    year,
                    month,
                    day
                )
                datePickerDialog.show()
            }
            return@setOnTouchListener true
        }

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
            clearErrors()
            if (validateProfileData()) {
                val about = editProfileBinding.aboutTi.editText?.text.toString()
                val dob = DateTimeUtils.localDateToIso(
                    editProfileBinding.dob.editText?.text.toString()
                )
                profileViewModel.updateUserProfile(
                    userId!!,
                    UpdateUserProfileRequest(
                        about,
                        languages.ifEmpty { null },
                        dob,
                        null,
                        null,
                        if (userLocation != null) {
                            userLocation
                        } else null,
                        skills.ifEmpty { null }
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
        getUserProfileLoadingObserver()
        getUserProfileErrorObserver()
        updateUserProfileLoadingObserver()
        updateUserProfileObserver()
        updateUserProfileErrorObserver()
        getFileObserver()
        getFileErrorObserver()
    }

    private fun clearErrors() {
        editProfileBinding.aboutTi.error = ""
        editProfileBinding.dob.error = ""
        editProfileBinding.errorTv.visibility = View.INVISIBLE
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
            val locationResult = if (((it.address.city
                    ?: it.address.state) + ", " + it.address.country).replace(", ", "").isEmpty()
            ) {
                ""
            } else {
                (it.address.city ?: it.address.state) + ", " + it.address.country
            }

            editProfileBinding.searchView.setQuery(locationResult, false)
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
            editProfileBinding.aboutTi.editText?.setText(it.profile.about.ifEmpty { " " })
            editProfileBinding.dob.editText?.setText(DateTimeUtils.stringToLocalDate(it.profile.dob))
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

    private fun getUserProfileLoadingObserver() {
        profileViewModel.loadingUserProfile.observe(this) {
            editProfileBinding.progressBar.visibility = if (it) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
        }
    }

    private fun getUserProfileErrorObserver() {
        profileViewModel.errorUserProfile.observe(this) {
            Snackbar.make(editProfileBinding.root, it.message!!, Snackbar.LENGTH_SHORT).show()
        }
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
        profileViewModel.updateUserProfileResponse.observe(this) {
            Snackbar.make(
                editProfileBinding.root,
                "Profile Updated successfully",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateUserProfileErrorObserver() {
        profileViewModel.errorUpdateUserProfile.observe(this) {
            editProfileBinding.errorTv.visibility = View.VISIBLE
            editProfileBinding.errorTv.text = it.message!!
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
        val about = editProfileBinding.aboutTi.editText?.text.toString()
        val dob = editProfileBinding.dob.editText?.text.toString()

        if (about.isEmpty() && isAboutUpdated) {
            editProfileBinding.aboutTi.error = "About is a required field."
            return false
        }

        if (dob.isEmpty()) {
            editProfileBinding.dob.error = "Date of birth is a required field."
            return false
        }

        return true
    }
}