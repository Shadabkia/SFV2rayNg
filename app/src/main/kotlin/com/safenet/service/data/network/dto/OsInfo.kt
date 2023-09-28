package com.safenet.service.data.network.dto

data class OsInfo(
    val osName : String? = "Android",
    val osVersion: String,
    val architecture : String,
    val appVersion : String,

) {
//    override fun toString(): String {
//        return "{"+"osName"+ ":" + "$osName"+","+'osVersion' : '$osVersion','architecture' : '$architecture','appVersion' : '$appVersion'}"
//    }
}

