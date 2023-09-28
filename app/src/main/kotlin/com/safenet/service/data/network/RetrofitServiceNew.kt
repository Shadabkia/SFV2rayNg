package com.safenet.service.data.network

import com.safenet.service.data.network.dto.UpdateLinkResponse
import retrofit2.Response
import retrofit2.http.POST

interface RetrofitServiceNew {

    @POST("baseAddress")
    suspend fun getBaseAddress(
    ): Response<UpdateLinkResponse>

}