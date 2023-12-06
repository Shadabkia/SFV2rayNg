package com.safenet.service.data.network.dto

data class ServerListResponse(
    val list: List<Server>,
    val status: Status
)