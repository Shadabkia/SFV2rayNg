package com.safenet.service.data.network

import com.safenet.service.data.network.dto.ConfigResponse
import com.safenet.service.data.network.dto.LoginResponse
import com.safenet.service.data.network.dto.ServerListResponse
import com.safenet.service.data.network.dto.UpdateLinkRequest
import com.safenet.service.data.network.dto.UpdateLinkResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RetrofitService {

    // field force should be 0 or 1
    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("publicU") publicU: String,
        @Field("osInfo") osInfo: String,
        @Field("force") force: Int
    ): Response<LoginResponse>

    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("email") email: String?,
        @Field("referral") referral: String?,
        @Field("publicU") publicU: String,
        @Field("osInfo") osInfo: String,
    ): Response<LoginResponse>

    @FormUrlEncoded
    @POST("connect")
    suspend fun getConfig(
        @Field("token") token: String,
        @Field("serverNumber") serverNumber: Int,
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
    @POST("serverList")
    suspend fun getServerList(
        @Field("token") token: String,
    ): Response<ServerListResponse>

}