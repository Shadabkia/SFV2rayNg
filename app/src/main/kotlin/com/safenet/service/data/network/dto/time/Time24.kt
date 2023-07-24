package com.safenet.service.data.network.dto.time


import com.google.gson.annotations.SerializedName

data class Time24(
    @SerializedName("full")
    val full: Full,
    @SerializedName("hour")
    val hour: Hour,
    @SerializedName("minute")
    val minute: Minute,
    @SerializedName("second")
    val second: Second
)