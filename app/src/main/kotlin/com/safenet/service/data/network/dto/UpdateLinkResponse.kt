package com.safenet.service.data.network.dto


import com.google.gson.annotations.SerializedName

data class UpdateLinkResponse(
    @SerializedName("status")
    val status: Status,
    @SerializedName("link")
    val link: String,
    @SerializedName("required")
    val required: Int
)