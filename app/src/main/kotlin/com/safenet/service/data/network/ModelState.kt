package com.safenet.service.data.network

import javax.annotation.concurrent.Immutable

@Immutable
data class ModelState<T>(
    val isLoading: Boolean = false,
    var response: T? = null,
    val error: String = ""
)