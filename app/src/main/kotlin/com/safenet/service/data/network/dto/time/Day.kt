package com.safenet.service.data.network.dto.time


import com.google.gson.annotations.SerializedName

data class Day(
    @SerializedName("name")
    val name: String,
    @SerializedName("events")
    val events: Events,
    @SerializedName("number")
    val number: Number
)