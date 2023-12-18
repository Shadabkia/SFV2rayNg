package com.safenet.service.data.network

import com.safenet.service.data.network.dto.ConfigResponse
import com.safenet.service.data.network.dto.ServerListResponse
import com.safenet.service.data.network.dto.Status
import com.safenet.service.data.network.dto.UpdateLinkRequest
import com.safenet.service.data.network.dto.UpdateLinkResponse
import com.safenet.service.data.network.dto.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RetrofitService {

    @FormUrlEncoded
    @POST("verify")
    suspend fun verifyVoucher(
        @Field("voucher") voucher: String,
        @Field("publicU") publicU: String,
        @Field("osInfo") osInfo: String,
        @Field("force") force: Int
    ): Response<RegisterResponse>

    // field force should be 0 or 1
    @FormUrlEncoded
    @POST("login")
    suspend fun verifyVoucher(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("publicU") publicU: String,
        @Field("osInfo") osInfo: String,
        @Field("force") force: Int
    ): Response<RegisterResponse>

    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("referral") referral: String,
        @Field("telegramID") telegramID: String,
        @Field("publicU") publicU: String,
        @Field("osInfo") osInfo: String,
    ): Response<RegisterResponse>

    @FormUrlEncoded
    @POST("config")
    suspend fun getConfig(
        @Field("token") token: String,
        @Field("serverNumber") serverNumber: Int
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

    @FormUrlEncoded
    @POST("getServerList")
    suspend fun getServerList(
        @Field("token") token: String,
    ): Response<ServerListResponse>

}