package com.safenet.service.ui.server_bottomsheet

sealed class ServerListBottomSheetEvents{
    class NavigateToEnterCode(val code : String) : ServerListBottomSheetEvents()
    object InitViews: ServerListBottomSheetEvents()
    object Success: ServerListBottomSheetEvents()
    class Error(val message : String): ServerListBottomSheetEvents()

}
