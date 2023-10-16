package com.arkindustries.gogreen.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.databinding.FragmentPortfolioBinding
import com.arkindustries.gogreen.ui.adapters.ProjectsAdapter
import com.arkindustries.gogreen.ui.repositories.ProfileRepository
import com.arkindustries.gogreen.ui.viewmodels.ProfileViewModel
import com.arkindustries.gogreen.ui.viewmodels.factory.ProfileViewModelFactory

class PortfolioFragment : Fragment() {
    private lateinit var portfolioBinding: FragmentPortfolioBinding
    private lateinit var projectsAdapter: ProjectsAdapter
    private lateinit var profileRepository: ProfileRepository
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        portfolioBinding = FragmentPortfolioBinding.inflate(inflater, container, false)
        return portfolioBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        projectsAdapter = ProjectsAdapter { }
        portfolioBinding.portfolioRv.adapter = projectsAdapter

        val profileService = RetrofitClient.createProfileService(requireContext())
        profileRepository = ProfileRepository(profileService)
        profileViewModel = ViewModelProvider(
            this,
            ProfileViewModelFactory(profileRepository)
        )[ProfileViewModel::class.java]

        parentFragmentManager.setFragmentResultListener("portfolio", this) { _, bundle ->
            val userId = bundle.getString("userId")
            profileViewModel.getUserProfile(userId!!)
            profileObserver()
        }
    }

    private fun profileObserver() {
        profileViewModel.userProfileResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                val projects = it.profile.projects
                if (projects.isEmpty()) {
                    portfolioBinding.noProjects.visibility = View.VISIBLE
                } else {
                    portfolioBinding.noProjects.visibility = View.GONE
                    projectsAdapter.submitList(projects)
                }
            }
        }
    }
}