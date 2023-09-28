package com.safenet.service.data.network

import com.google.gson.annotations.SerializedName

data class AuthErrorResponse(
    @SerializedName("error")
    val error: String,
    @SerializedName("error_description")
    val errorDescription: String
)