package com.safenet.service.ui

import com.safenet.service.R
import com.safenet.service.util.Utils
import android.os.Bundle
import com.safenet.service.service.V2RayServiceManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ScSwitchActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moveTaskToBack(true)

        setContentView(R.layout.activity_none)

        if (V2RayServiceManager.v2rayPoint.isRunning) {
            Utils.stopVService(this)
        } else {
            Timber.d("startService 4")
            Utils.startVServiceFromToggle(this)
        }
        finish()
    }
}
