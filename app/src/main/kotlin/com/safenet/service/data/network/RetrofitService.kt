package com.safenet.service.data.network

import com.safenet.service.data.network.dto.ConfigResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface RetrofitService {

//
    @FormUrlEncoded
    @POST("/api/getconfig")
    suspend fun login(@Field("username") username: String,
                      @Field("password") password : String,
                      @Field("client_id") client_id: String,
                      @Field("client_secret") client_secret: String,
                      @Field("grant_type") grant_type: String,
    ): Response<ConfigResponse>
//
//    @GET("/")
//    suspend fun getUserInfo(): Response<String>
//
//    @FormUrlEncoded
//    @POST("/")
//    suspend fun getRegisterVerificationCode(@Field("username") username: String,
//                                            @Field("password") password : String,
//                                            @Field("client_id") client_id: String,
//                                            @Field("client_secret") client_secret: String,
//                                            @Field("grant_type") grant_type: String,
//
//    ): Response<PreConfirmationCustomer>
}