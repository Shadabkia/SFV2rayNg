package com.safenet.service.data.network.dto


import com.google.gson.annotations.SerializedName

data class ConfigResponse(
    @SerializedName("config")
    val config: String,
)