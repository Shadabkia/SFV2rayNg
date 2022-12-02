package com.safenet.service.data.repository

import com.safenet.service.data.network.RetrofitService
import com.safenet.service.data.network.SafeApiRequest
import javax.inject.Inject

class VerificationRepositoryImpl @Inject constructor(
    private val api : RetrofitService,
) : VerificationRepository, SafeApiRequest(){
    override fun verifyVoucher(voucher: String, publicIdU: String) =
        apiRequest {
            api.verifyVoucher(voucher, publicIdU)
        }
}