package com.safenet.service.ui.on_boarding.register

sealed class RegisterEvents{
    class NavigateToEnterCode(val code : String) : RegisterEvents()
    data object InitViews: RegisterEvents()
    data object Success: RegisterEvents()
    data object Error: RegisterEvents()
}
