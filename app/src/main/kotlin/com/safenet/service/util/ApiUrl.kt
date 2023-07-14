package com.safenet.service.util

import com.safenet.service.BuildConfig
import com.safenet.service.data.local.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

object ApiUrl {
    var base_url_counter = MutableStateFlow(0)
    var BASE_URL = BuildConfig.BASE_URL
}