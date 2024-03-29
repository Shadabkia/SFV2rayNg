package com.safenet.service.util

import com.safenet.service.BuildConfig
import com.safenet.service.data.local.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

object ApiUrl {
    var base_url_counter = MutableStateFlow(0)
    var BASE_URL = "https://net.safenetvpn.cf:3028/api/app/"
    var New_BASE_URL = "https://safenetapp.ekcal.com:3047/api/app/"
    var TIME_URL = "https://api.keybit.ir/"
}