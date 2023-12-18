package com.safenet.service.ui.main

import java.io.File

sealed class MainActivityEvents {
    object ShowLogoutDialog : MainActivityEvents()
    class ShowMessageDialog(val message : String?) : MainActivityEvents()
    object MaxLoginDialog : MainActivityEvents()
    object HideCircle : MainActivityEvents()
    object DownloadStarted : MainActivityEvents()
    object DownloadFailed : MainActivityEvents()
    object InitViews : MainActivityEvents()

    class OpenBrowser(val link : String) : MainActivityEvents()

    class ActivateApp(val status : Boolean) : MainActivityEvents()
    class GetConfigMessage(val message : String?) : MainActivityEvents()
    class Disconnected(val message: String) : MainActivityEvents()
    class ShowMessage(val message: String) : MainActivityEvents()
    object ShowTimeDialog : MainActivityEvents()

    class ShowUpdateUI(val status: Boolean) : MainActivityEvents()
    class DownloadFinished(val progress: Int, val file : File) : MainActivityEvents()
}