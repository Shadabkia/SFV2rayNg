package com.safenet.service.ui.on_boarding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import com.safenet.service.R
import com.safenet.service.databinding.ActivityOnBoardingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnBoardingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initViews()
    }

    private fun initViews() {
        binding.apply {
            val adapter = OnBoardingSlidePagerAdapter(this@OnBoardingActivity)
            vp2OnBoarding.adapter = adapter
            vp2OnBoarding.isUserInputEnabled = false
            TabLayoutMediator(tlOnBoarding, vp2OnBoarding) { tab, position ->
                tab.text = when(position){
                    0 -> getString(R.string.login)
                    else -> getString(R.string.register)
                }
            }.attach()
        }
    }
}