package com.safenet.service.data.repository

import com.safenet.service.data.network.dto.VerifyResponse
import kotlinx.coroutines.flow.Flow
import com.safenet.service.data.network.Result
import com.safenet.service.data.network.dto.ConfigResponse
import com.safenet.service.data.network.dto.UpdateLinkRequest
import com.safenet.service.data.network.dto.UpdateLinkResponse
import com.safenet.service.data.network.dto.time.TimeResponse

interface VerificationRepository {
    fun setBaseUrl(baseUrl : String)
    fun verifyVoucher(username: String, password: String, publicIdU : String, osInfo: String, force: Int) : Flow<Result<VerifyResponse>>
    fun getConfig(token : String) : Flow<Result<ConfigResponse>>
    fun disconnect(token : String) : Flow<Result<ConfigResponse>>
    fun logout(token: String) : Flow<Result<ConfigResponse>>
    fun getUpdateLink(req : UpdateLinkRequest) : Flow<Result<UpdateLinkResponse>>
    fun getBaseAddress() : Flow<Result<UpdateLinkResponse>>
    fun getTime() : Flow<Result<TimeResponse>>
}

