package com.arkindustries.gogreen.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ProfileViewPagerAdapter(
    fragment: Fragment,
    private val fragments: List<Fragment>,
    private val fallBackPosition: Int = 0
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments [getAdjustedPosition(position)]
    }

    private fun getAdjustedPosition (position: Int): Int {
        return if (position < fragments.size) {
            position
        } else {
            fallBackPosition
        }
    }
}