package com.safenet.service.data.network.dto.time


import com.google.gson.annotations.SerializedName

data class TimeResponse(
    @SerializedName("unix")
    val unix: Unix,
    @SerializedName("timestamp")
    val timestamp: Timestamp,
    @SerializedName("timezone")
    val timezone: Timezone,
    @SerializedName("season")
    val season: Season,
    @SerializedName("time12")
    val time12: Time12,
    @SerializedName("time24")
    val time24: Time24,
    @SerializedName("date")
    val date: Date
)