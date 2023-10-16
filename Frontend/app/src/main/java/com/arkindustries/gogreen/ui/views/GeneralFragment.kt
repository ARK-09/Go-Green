package com.arkindustries.gogreen.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.databinding.FragmentGeneralBinding
import com.arkindustries.gogreen.ui.adapters.LanguageAdapter
import com.arkindustries.gogreen.ui.repositories.ProfileRepository
import com.arkindustries.gogreen.ui.viewmodels.ProfileViewModel
import com.arkindustries.gogreen.ui.viewmodels.factory.ProfileViewModelFactory

class GeneralFragment : Fragment() {
    private lateinit var generalBinding: FragmentGeneralBinding
    private lateinit var profileRepository: ProfileRepository
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var languagesAdapter: LanguageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        generalBinding = FragmentGeneralBinding.inflate(inflater, container, false)
        return generalBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        languagesAdapter = LanguageAdapter { }
        val profileService = RetrofitClient.createProfileService(requireContext())
        profileRepository = ProfileRepository(profileService)
        profileViewModel = ViewModelProvider(
            this,
            ProfileViewModelFactory(profileRepository)
        )[ProfileViewModel::class.java]

        generalBinding.languageRv.adapter = languagesAdapter

        parentFragmentManager.setFragmentResultListener("general", this) { _, bundle ->
            val userId = bundle.getString("userId")
            profileViewModel.getUserProfile(userId!!)
            profileObserver()
        }
    }

    private fun profileObserver() {
        profileViewModel.userProfileResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                generalBinding.userAbout.text = it.profile.about
                languagesAdapter.submitList(it.profile.languages)
            }
        }
    }
}