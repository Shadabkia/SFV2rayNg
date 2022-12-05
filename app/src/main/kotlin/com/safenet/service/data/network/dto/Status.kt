package com.safenet.service.data.network.dto

import com.google.gson.annotations.SerializedName

data class Status(
    @SerializedName("code")
    val code: Int,
    @SerializedName("massage")
    val massage: String,
)