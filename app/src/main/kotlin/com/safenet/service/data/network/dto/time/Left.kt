package com.safenet.service.data.network.dto.time


import com.google.gson.annotations.SerializedName

data class Left(
    @SerializedName("days")
    val days: Days,
    @SerializedName("percent")
    val percent: Percent
)