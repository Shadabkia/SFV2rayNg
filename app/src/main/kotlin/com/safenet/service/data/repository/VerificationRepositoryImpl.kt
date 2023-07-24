package com.safenet.service.data.repository

import com.safenet.service.data.network.*
import com.safenet.service.data.network.dto.ConfigResponse
import com.safenet.service.data.network.dto.UpdateLinkRequest
import com.safenet.service.data.network.dto.UpdateLinkResponse
import com.safenet.service.data.network.dto.time.TimeResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VerificationRepositoryImpl @Inject constructor(
    private val api: RetrofitService,
    private val newApi: RetrofitServiceNew,
    private val timeApi : RetrofitServiceTime
) : VerificationRepository, SafeApiRequest() {
    override fun verifyVoucher(voucher: String, publicIdU: String, osInfo: String, force: Int) =
        apiRequest {
            api.verifyVoucher(voucher, publicIdU, osInfo, force)
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