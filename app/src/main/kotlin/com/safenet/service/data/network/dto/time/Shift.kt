package com.safenet.service.data.network.dto.time


import com.google.gson.annotations.SerializedName

data class Shift(
    @SerializedName("short")
    val short: String,
    @SerializedName("full")
    val full: String
)