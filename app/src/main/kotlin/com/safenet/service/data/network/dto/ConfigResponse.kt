package com.safenet.service.data.network.dto


import com.google.gson.annotations.SerializedName

data class ConfigResponse(
    @SerializedName("status")
    val status: Status,
    @SerializedName("config")
    val config: String,
    @SerializedName("lastVersion")
    val lastVersion: Int,
)