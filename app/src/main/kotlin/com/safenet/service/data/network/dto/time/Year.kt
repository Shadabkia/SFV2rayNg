package com.safenet.service.data.network.dto.time


import com.google.gson.annotations.SerializedName

data class Year(
    @SerializedName("name")
    val name: String,
    @SerializedName("animal")
    val animal: String,
    @SerializedName("leapyear")
    val leapyear: String,
    @SerializedName("agone")
    val agone: Agone,
    @SerializedName("left")
    val left: Left,
    @SerializedName("number")
    val number: Number
)