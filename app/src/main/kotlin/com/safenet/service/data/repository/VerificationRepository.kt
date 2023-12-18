package com.safenet.service.data.repository

import com.safenet.service.data.network.dto.RegisterResponse
import kotlinx.coroutines.flow.Flow
import com.safenet.service.data.network.Result
import com.safenet.service.data.network.dto.ConfigResponse
import com.safenet.service.data.network.dto.ServerListResponse
import com.safenet.service.data.network.dto.Status
import com.safenet.service.data.network.dto.UpdateLinkRequest
import com.safenet.service.data.network.dto.UpdateLinkResponse
import com.safenet.service.data.network.dto.time.TimeResponse

interface VerificationRepository {
    fun setBaseUrl(baseUrl : String)
    fun verifyVoucher(username: String, password: String, publicIdU : String, osInfo: String, force: Int) : Flow<Result<RegisterResponse>>
//    fun verifyVoucher(voucher: String, publicIdU : String, osInfo: String, force: Int) : Flow<Result<RegisterResponse>>
    fun register(username: String, password: String, referral: String, telegramId : String,  publicIdU : String, osInfo: String) : Flow<Result<RegisterResponse>>
    fun getConfig(token: String, serverNumber: Int) : Flow<Result<ConfigResponse>>
    fun disconnect(token : String) : Flow<Result<ConfigResponse>>
    fun logout(token: String) : Flow<Result<ConfigResponse>>
    fun getUpdateLink(req : UpdateLinkRequest) : Flow<Result<UpdateLinkResponse>>
    fun getBaseAddress() : Flow<Result<UpdateLinkResponse>>
    fun getTime() : Flow<Result<TimeResponse>>
    fun getServerList(token : String) : Flow<Result<ServerListResponse>>

}

