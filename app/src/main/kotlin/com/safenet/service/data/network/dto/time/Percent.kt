package com.safenet.service.data.network.dto.time


import com.google.gson.annotations.SerializedName

data class Percent(
    @SerializedName("fa")
    val fa: String,
    @SerializedName("en")
    val en: String
)