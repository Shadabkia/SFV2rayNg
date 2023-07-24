package com.safenet.service.data.network.dto.time


import com.google.gson.annotations.SerializedName

data class Full(
    @SerializedName("short")
    val short: Short,
    @SerializedName("full")
    val full: FullX
)