package com.safenet.service.data.network

import com.safenet.service.data.network.dto.ConfigResponse
import com.safenet.service.data.network.dto.VerifyResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface RetrofitService {

    //
    @FormUrlEncoded
    @POST("/api/verify")
    suspend fun verifyVoucher(
        @Field("voucher") voucher: String,
        @Field("publicU") publicU: String,
    ): Response<VerifyResponse>

    @FormUrlEncoded
    @POST("/api/config")
    suspend fun getConfig(
        @Field("token") token: String,
    ): Response<ConfigResponse>

}