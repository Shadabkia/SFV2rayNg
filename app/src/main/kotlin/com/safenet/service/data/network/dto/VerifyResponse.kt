package com.safenet.service.data.network.dto


import com.google.gson.annotations.SerializedName

data class VerifyResponse(
    @SerializedName("status")
    val status: Status,
    @SerializedName("token")
    val token: String,
    @SerializedName("public_s")
    val public_s: String
)

