package com.safenet.service.data.network.dto

import com.google.gson.annotations.SerializedName

data class LastVersion(
    @SerializedName("versionCode")
    val versionCode: String,
    @SerializedName("required")
    val required: Int = 0,
)
