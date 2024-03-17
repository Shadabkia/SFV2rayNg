package com.safenet.service.data.network.dto


import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("contactLink")
    val contactLink: String?,
    @SerializedName("defaultServer")
    val defaultServer: DefaultServer?,
    @SerializedName("publicS")
    val publicS: String,
    @SerializedName("status")
    val status: Status?,
    @SerializedName("token")
    val token: String
)