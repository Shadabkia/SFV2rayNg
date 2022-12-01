package com.safenet.service.data.network

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("code")
    val code: String,
    @SerializedName("message")
    val message: List<String>,
    @SerializedName("timeStamp")
    val timeStamp: String
)