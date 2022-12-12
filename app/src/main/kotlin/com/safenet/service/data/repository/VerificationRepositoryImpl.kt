package com.safenet.service.data.repository

import com.safenet.service.data.network.Result
import com.safenet.service.data.network.RetrofitService
import com.safenet.service.data.network.SafeApiRequest
import com.safenet.service.data.network.dto.ConfigResponse
import com.safenet.service.data.network.dto.Status
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VerificationRepositoryImpl @Inject constructor(
    private val api : RetrofitService,
) : VerificationRepository, SafeApiRequest(){
    override fun verifyVoucher(voucher: String, publicIdU: String) =
        apiRequest {
            api.verifyVoucher(voucher, publicIdU)
        }

    override fun getConfig(token: String): Flow<Result<ConfigResponse>> =
        apiRequest {
            api.getConfig(token = token)
        }

    override fun disconnect(): Flow<Result<Status>> =
        apiRequest {
            api.disconnect()
        }
}