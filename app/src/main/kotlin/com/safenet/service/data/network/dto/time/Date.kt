package com.safenet.service.data.network.dto.time


import com.google.gson.annotations.SerializedName

data class Date(
    @SerializedName("full")
    val full: FullXXX,
    @SerializedName("other")
    val other: Other,
    @SerializedName("year")
    val year: Year,
    @SerializedName("month")
    val month: Month,
    @SerializedName("day")
    val day: Day,
    @SerializedName("weekday")
    val weekday: Weekday
)