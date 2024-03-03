package com.safenet.service.ui.on_boarding.login

sealed class LoginEvents{
    class NavigateToEnterCode(val code : String) : LoginEvents()
    object InitViews: LoginEvents()
    object Success: LoginEvents()
    object Error: LoginEvents()
    object
    MaxUserDialog : LoginEvents()

    class MaxLoginDialog(val message : String) : LoginEvents()
}
