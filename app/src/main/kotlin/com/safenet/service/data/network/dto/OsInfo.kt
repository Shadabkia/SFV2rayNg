package com.safenet.service.data.network.dto

data class OsInfo(
    val osName : Int = 0,
    val osVersion: String,
    val architecture : String,
    val androidID : String,
    val appVersion : Int

) {
//    override fun toString(): String {
//        return "{"+"osName"+ ":" + "$osName"+","+'osVersion' : '$osVersion','architecture' : '$architecture','appVersion' : '$appVersion'}"
//    }
}

