package com.safenet.service.ui.on_boarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.safenet.service.ui.on_boarding.login.LoginFragment
import com.safenet.service.ui.on_boarding.register.RegisterFragment

class OnBoardingSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                LoginFragment()
            }

            else -> {
                RegisterFragment()
            }
        }
    }
}