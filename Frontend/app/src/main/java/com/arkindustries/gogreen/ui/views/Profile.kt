package com.arkindustries.gogreen.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.transition.TransitionInflater
import androidx.viewpager2.widget.ViewPager2
import com.arkindustries.gogreen.AppContext
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.databinding.FragmentProfileBinding
import com.arkindustries.gogreen.ui.adapters.ProfileViewPagerAdapter
import com.arkindustries.gogreen.ui.bindingadapters.image
import com.arkindustries.gogreen.ui.bindingadapters.location
import com.arkindustries.gogreen.ui.repositories.ProfileRepository
import com.arkindustries.gogreen.ui.viewmodels.ProfileViewModel
import com.arkindustries.gogreen.ui.viewmodels.factory.ProfileViewModelFactory
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class Profile : Fragment() {
    private lateinit var profileBinding: FragmentProfileBinding
    private lateinit var profileViewPager: ViewPager2
    private lateinit var profileTabLayout: TabLayout
    private lateinit var profileRepository: ProfileRepository
    private lateinit var profileViewModel: ProfileViewModel
    private var isProfileOwnerAClient: Boolean = false
    private var isProfileOwner: Boolean = true
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val profileService = RetrofitClient.createProfileService(requireContext())
        profileRepository = ProfileRepository(profileService)
        profileViewModel = ViewModelProvider(this, ProfileViewModelFactory (profileRepository))[ProfileViewModel::class.java]

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        profileBinding = FragmentProfileBinding.inflate(inflater, container, false)
        enterTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.slide_out)
        exitTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.slide_in)
        return profileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        profileViewPager = profileBinding.tabViewPager
        profileTabLayout = profileBinding.tabLayout

        parentFragmentManager.setFragmentResultListener("profile", this) { _, bundle ->
            isProfileOwner = bundle.getBoolean("isProfileOwner")
            userId = bundle.getString("userId")
            profileViewModel.getUserProfile(userId!!)
            profileObserver ()
        }

        if (userId == null) {
            userId = AppContext.getInstance().currentUser.userId
            profileViewModel.getUserProfile(userId!!)
            profileObserver ()
        }
    }


    private fun profileObserver () {
        profileViewModel.userProfileResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                isProfileOwnerAClient = it.profile.user.userType == "client"
                init()
                profileBinding.userName.text = it.profile.user.name
                profileBinding.userLocation.location(it.profile.location)
                profileBinding.userIv.image(it.profile.user.image.url, R.drawable.test)
                profileBinding.userRatting.text = it.profile.rating.toString()
                profileBinding.userCompletedProjects.text = it.profile.projects.size.toString()
                profileBinding.userRank.text = it.profile.ranking.replace("_", " ")
            }
        }
    }

    private fun init () {
        val bundle = bundleOf()
        bundle.putString("userId", userId)

        childFragmentManager.setFragmentResult("portfolio", bundle)
        childFragmentManager.setFragmentResult("general", bundle)
        childFragmentManager.setFragmentResult("reviews", bundle)

        val fragments = getFragmentsList()
        profileViewPager.adapter = ProfileViewPagerAdapter(this, fragments, 0)
        val currentItem = if (fragments.size < 3)  0 else 1
        profileViewPager.setCurrentItem(currentItem, true)

        TabLayoutMediator(profileTabLayout, profileViewPager) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()
    }

    private fun getFragmentsList(): List<Fragment> {
        val fragments = mutableListOf<Fragment>()

        if ((isProfileOwner && !isProfileOwnerAClient) || (!isProfileOwner && !isProfileOwnerAClient)) {
            fragments.add(PortfolioFragment())
        }

        fragments.add(GeneralFragment())
        fragments.add(ReviewsFragment())

        return fragments
    }

    private fun getTabTitle(position: Int): String {
        return if ((isProfileOwner && !isProfileOwnerAClient) || (!isProfileOwner && !isProfileOwnerAClient)) {
            when (position) {
                0 -> "Portfolio"
                1 -> "General"
                2 -> "Reviews"
                else -> "Portfolio"
            }
        } else {
            when (position) {
                0 -> "General"
                1 -> "Reviews"
                else -> "Reviews"
            }
        }
    }
}