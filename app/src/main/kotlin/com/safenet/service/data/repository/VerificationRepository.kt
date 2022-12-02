package com.safenet.service.data.repository

import com.safenet.service.data.network.dto.VerifyResponse
import kotlinx.coroutines.flow.Flow

interface VerificationRepository {
    fun verifyVoucher(voucher: String,  publicIdU : String) : Flow<com.safenet.service.data.network.Result<VerifyResponse>>
}