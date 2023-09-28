package com.safenet.service.data.network.dto.time


import com.google.gson.annotations.SerializedName

data class FullXXX(
    @SerializedName("official")
    val official: Official,
    @SerializedName("unofficial")
    val unofficial: Unofficial
)