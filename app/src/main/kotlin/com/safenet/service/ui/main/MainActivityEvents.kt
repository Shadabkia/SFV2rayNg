package com.safenet.service.ui.main

sealed class MainActivityEvents {
    object ShowLogoutDialog : MainActivityEvents()
    object MaxLoginDialog : MainActivityEvents()
    object HideCircle : MainActivityEvents()
    class ActivateApp(val status : Boolean) : MainActivityEvents()
    class GetConfigMessage(val message : String?) : MainActivityEvents()
    class Disconnected(val message: String) : MainActivityEvents()
    class ShowMessage(val message: String) : MainActivityEvents()
}