package com.safenet.service.data.network

import com.safenet.service.data.network.dto.ConfigResponse
import com.safenet.service.data.network.dto.UpdateLinkRequest
import com.safenet.service.data.network.dto.UpdateLinkResponse
import com.safenet.service.data.network.dto.VerifyResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RetrofitService {

    // field force should be 0 or 1
    @FormUrlEncoded
    @POST("login")
    suspend fun verifyVoucher(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("publicU") publicU: String,
        @Field("osInfo") osInfo: String,
        @Field("force") force: Int
    ): Response<VerifyResponse>

    @FormUrlEncoded
    @POST("config")
    suspend fun getConfig(
        @Field("token") token: String,
    ): Response<ConfigResponse>

    @FormUrlEncoded
    @POST("disconnect")
    suspend fun disconnect(
        @Field("token") token: String,
    ): Response<ConfigResponse>

    @FormUrlEncoded
    @POST("logout")
    suspend fun logout(
        @Field("token") token: String,
    ): Response<ConfigResponse>

    @POST("updateLink")
    suspend fun getUpdateLink(
        @Body req : UpdateLinkRequest
    ): Response<UpdateLinkResponse>


}