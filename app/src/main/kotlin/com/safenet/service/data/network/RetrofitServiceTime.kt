package com.safenet.service.data.network

import com.safenet.service.data.network.dto.UpdateLinkResponse
import com.safenet.service.data.network.dto.time.TimeResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface RetrofitServiceTime {

    @POST("time")
    suspend fun getTime(
    ): Response<TimeResponse>

}