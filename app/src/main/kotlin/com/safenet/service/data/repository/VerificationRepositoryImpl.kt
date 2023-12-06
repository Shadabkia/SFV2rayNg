package com.safenet.service.data.repository

import com.safenet.service.data.network.*
import com.safenet.service.data.network.dto.ConfigResponse
import com.safenet.service.data.network.dto.UpdateLinkRequest
import com.safenet.service.data.network.dto.UpdateLinkResponse
import com.safenet.service.data.network.dto.time.TimeResponse
import com.safenet.service.di.RetrofitFactory
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import javax.inject.Inject

class VerificationRepositoryImpl @Inject constructor(
    var retrofit: RetrofitFactory,
    var api: RetrofitService,
    var newApi: RetrofitServiceNew,
    var timeApi : RetrofitServiceTime
) : VerificationRepository, SafeApiRequest() {


    lateinit var newRetrofit: Retrofit

    override fun setBaseUrl(baseUrl: String) {
        if (baseUrl.isNotEmpty()) {
            newRetrofit  = retrofit.create(baseUrl+":3028/api/app/")
            api = newRetrofit.create(RetrofitService::class.java)
        }
    }

    override fun verifyVoucher(
        username: String,
        password: String,
        publicIdU: String,
        osInfo: String,
        force: Int
    ) =
        apiRequest {
            api.verifyVoucher(username, password, publicIdU, osInfo, force)
        }

    override fun getConfig(token: String): Flow<Result<ConfigResponse>> =
        apiRequest {
            api.getConfig(token = token)
        }

    override fun disconnect(token: String): Flow<Result<ConfigResponse>> =
        apiRequest {
            api.disconnect(token = token)
        }

    override fun logout(token: String): Flow<Result<ConfigResponse>> =
        apiRequest {
            api.logout(token)
        }

    override fun getUpdateLink(
        req: UpdateLinkRequest
    ): Flow<Result<UpdateLinkResponse>> =
        apiRequest {
            api.getUpdateLink(req)
        }

    override fun getBaseAddress(): Flow<Result<UpdateLinkResponse>> =
        apiRequest {
            newApi.getBaseAddress()
        }

    override fun getTime(): Flow<Result<TimeResponse>> =
        apiRequest {
            timeApi.getTime()
        }
}