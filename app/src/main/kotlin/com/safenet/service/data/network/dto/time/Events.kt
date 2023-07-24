package com.safenet.service.data.network.dto.time


import com.google.gson.annotations.SerializedName

data class Events(
    @SerializedName("local")
    val local: Any,
    @SerializedName("holy")
    val holy: Any,
    @SerializedName("global")
    val global: Any
)