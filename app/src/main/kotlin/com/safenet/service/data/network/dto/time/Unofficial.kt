package com.safenet.service.data.network.dto.time


import com.google.gson.annotations.SerializedName

data class Unofficial(
    @SerializedName("iso")
    val iso: Iso,
    @SerializedName("usual")
    val usual: Usual
)