package com.safenet.service.ui.main

sealed class MainActivityEvents {
    class ActivateApp(val status : Boolean) : MainActivityEvents()
    class GetConfigMessage(val message : String?) : MainActivityEvents()
}