package com.safenet.service.data.network.dto.time


import com.google.gson.annotations.SerializedName

data class Other(
    @SerializedName("gregorian")
    val gregorian: Gregorian,
    @SerializedName("ghamari")
    val ghamari: Ghamari
)