package com.safenet.service.data.network.dto.time


import com.google.gson.annotations.SerializedName

data class Month(
    @SerializedName("name")
    val name: String,
    @SerializedName("asterism")
    val asterism: String,
    @SerializedName("number")
    val number: Number
)