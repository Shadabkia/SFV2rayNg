package com.safenet.service.data.network.dto.time


import com.google.gson.annotations.SerializedName

data class Season(
    @SerializedName("name")
    val name: String,
    @SerializedName("number")
    val number: Number
)