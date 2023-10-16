package com.arkindustries.gogreen.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.arkindustries.gogreen.ui.views.GeneralFragment
import com.arkindustries.gogreen.ui.views.PortfolioFragment
import com.arkindustries.gogreen.ui.views.ReviewsFragment

class ProfileOwnerViewPagerAdapter(
    fragment: Fragment,
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PortfolioFragment()
            1 -> GeneralFragment()
            2 -> ReviewsFragment()
            else -> PortfolioFragment()
        }
    }
}