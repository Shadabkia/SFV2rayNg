package com.safenet.service.data.network.dto.time


import com.google.gson.annotations.SerializedName

data class Time12(
    @SerializedName("full")
    val full: Full,
    @SerializedName("hour")
    val hour: Hour,
    @SerializedName("minute")
    val minute: Minute,
    @SerializedName("second")
    val second: Second,
    @SerializedName("microsecond")
    val microsecond: Microsecond,
    @SerializedName("shift")
    val shift: Shift
)