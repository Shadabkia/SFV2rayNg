package com.safenet.service.data.repository

import com.safenet.service.data.network.dto.VerifyResponse
import kotlinx.coroutines.flow.Flow
import com.safenet.service.data.network.Result
import com.safenet.service.data.network.dto.ConfigResponse

interface VerificationRepository {
    fun verifyVoucher(voucher: String,  publicIdU : String, osInfo: String, force: Int) : Flow<Result<VerifyResponse>>
    fun getConfig(token : String, osInfo: String) : Flow<Result<ConfigResponse>>
    fun disconnect(token : String) : Flow<Result<ConfigResponse>>
    fun logout(token: String) : Flow<Result<ConfigResponse>>
}