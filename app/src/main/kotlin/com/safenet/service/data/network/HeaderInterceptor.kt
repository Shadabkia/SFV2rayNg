package com.safenet.service.data.network

import com.safenet.service.AppConfig.APPLICATION_JSON_HEADER_KEY
import com.safenet.service.AppConfig.AUTHORIZATION_HEADER_KEY
import com.safenet.service.AppConfig.CONTENT_TYPE_HEADER_KEY
import com.safenet.service.data.local.DataStoreManager
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject

class HeaderInterceptor @Inject constructor(
    private val dataStoreManager: DataStoreManager
): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        requestBuilder.addHeader(
            CONTENT_TYPE_HEADER_KEY, APPLICATION_JSON_HEADER_KEY
        )
        dataStoreManager.getData(DataStoreManager.PreferenceKeys.ACCESS_TOKEN)
            .let {
                requestBuilder.addHeader(
                    AUTHORIZATION_HEADER_KEY, "Bearer $it"
                )
                Timber.tag("okhttp Header: ").i("Token: Bearer $it")
            }

        val request = requestBuilder.build()

        return chain.proceed(request)
    }
}