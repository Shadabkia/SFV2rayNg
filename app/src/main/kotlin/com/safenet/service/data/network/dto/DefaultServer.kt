package com.safenet.service.data.network.dto


import com.google.gson.annotations.SerializedName

data class DefaultServer(
    @SerializedName("name")
    val name: String?,
    @SerializedName("number")
    val number: Int?
)