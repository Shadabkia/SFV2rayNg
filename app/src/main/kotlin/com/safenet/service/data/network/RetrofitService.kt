package com.safenet.service.data.network

import com.safenet.service.data.network.dto.ConfigResponse
import com.safenet.service.data.network.dto.Status
import com.safenet.service.data.network.dto.VerifyResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RetrofitService {

    // field force should be 0 or 1
    @FormUrlEncoded
    @POST("/api/verify")
    suspend fun verifyVoucher(
        @Field("voucher") voucher: String,
        @Field("publicU") publicU: String,
        @Field("osInfo") osInfo: String,
        @Field("force") force: Int
    ): Response<VerifyResponse>

    @FormUrlEncoded
    @POST("/api/config")
    suspend fun getConfig(
        @Field("token") token: String,
        @Field("osInfo") osInfo: String
    ): Response<ConfigResponse>

    @FormUrlEncoded
    @POST("/api/disconnect")
    suspend fun disconnect(
        @Field("token") token: String,
    ): Response<Status>

    @FormUrlEncoded
    @POST("/api/logout")
    suspend fun logout(
        @Field("token") token: String,
    ): Response<Status>

}