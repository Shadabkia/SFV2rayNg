package com.safenet.service.data.network.dto.time


import com.google.gson.annotations.SerializedName

data class Weekday(
    @SerializedName("name")
    val name: String,
    @SerializedName("champ")
    val champ: String,
    @SerializedName("number")
    val number: Number
)