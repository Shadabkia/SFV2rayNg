package com.safenet.service.data.network.dto

import com.google.gson.annotations.SerializedName

data class LastVersion(
    @SerializedName("versionCode")
    val versionCode: Int,
    @SerializedName("required")
    val required: Int = 0,
)
