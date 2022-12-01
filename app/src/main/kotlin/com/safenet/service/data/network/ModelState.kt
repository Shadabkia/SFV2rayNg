package com.safenet.service.data.network

data class ModelState<T>(
    val isLoading: Boolean = false,
    var response: T? = null,
    val error: String = ""
)