package com.safenet.service.ui.main

import android.content.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.safenet.service.AngApplication
import com.safenet.service.AppConfig
import com.safenet.service.AppConfig.ANG_PACKAGE
import com.safenet.service.BuildConfig
import com.safenet.service.R
import com.safenet.service.data.local.DataStoreManager
import com.safenet.service.data.local.DataStoreManager.PreferenceKeys.ACCESS_TOKEN
import com.safenet.service.data.local.DataStoreManager.PreferenceKeys.IS_CONNECTED
import com.safenet.service.data.local.DataStoreManager.PreferenceKeys.PUBLIC_S
import com.safenet.service.data.local.DataStoreManager.PreferenceKeys.SERVER_ID
import com.safenet.service.data.network.Result
import com.safenet.service.data.network.dto.ConfigResponse
import com.safenet.service.data.network.dto.UpdateLinkRequest
import com.safenet.service.data.repository.VerificationRepository
import com.safenet.service.dto.EConfigType
import com.safenet.service.dto.ServerConfig
import com.safenet.service.dto.ServersCache
import com.safenet.service.dto.V2rayConfig
import com.safenet.service.extension.toast
import com.safenet.service.extension.toastLong
import com.safenet.service.service.V2RayServiceManager
import com.safenet.service.ui.server_bottomsheet.ServerListBottomSheetDialog
import com.safenet.service.ui.voucher_bottomsheet.EnterVoucherBottomSheetDialog
import com.safenet.service.ui.voucher_bottomsheet.EnterVoucherBottomSheetViewModel
import com.safenet.service.util.*
import com.safenet.service.util.ApiUrl.base_url_counter
import com.safenet.service.util.MmkvManager.KEY_ANG_CONFIGS
import com.tencent.mmkv.MMKV
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val verificationRepository: VerificationRepository,
    val application: AngApplication,
    val dataStoreManager: DataStoreManager,
    val savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val mainStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_MAIN,
            MMKV.MULTI_PROCESS_MODE
        )
    }
    private val serverRawStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_SERVER_RAW,
            MMKV.MULTI_PROCESS_MODE
        )
    }

    var serverList = MmkvManager.decodeServerList()
    var subscriptionId: String = ""
    var keywordFilter: String = ""
        private set
    val serversCache = mutableListOf<ServersCache>()
    val isRunning by lazy { MutableLiveData<Boolean>() }
    val updateListAction by lazy { MutableLiveData<Int>() }
    val updateTestResultAction by lazy { MutableLiveData<String>() }

    var appFileName = "safenet.apk"

    private val _serverAvailability = MutableStateFlow<String?>("servers")
    val serverAvailability: StateFlow<String?> get() = _serverAvailability

    private val _downloadPercentage = MutableStateFlow<Int?>(null)
    val downloadPercentage: StateFlow<Int?> get() = _downloadPercentage

    var config = MutableStateFlow("")
        private set

    private val mainActivityEventChannel = Channel<MainActivityEvents>()
    val mainActivityEvent = mainActivityEventChannel.receiveAsFlow()

    private val tcpingTestScope by lazy { CoroutineScope(Dispatchers.IO) }

    var isUpdateRequired = MutableStateFlow(false)

    private var isAppActive = false

    fun activityCreated() = viewModelScope.launch {
        mainActivityEventChannel.send(MainActivityEvents.InitViews)
        checkAppActivated()
        dataStoreManager.getData(DataStoreManager.PreferenceKeys.IS_UPDATE_MODE).collectLatest {
            it?.let {
                Timber.tag("showUpdateUI").d("showUpdateUI IS_UPDATE_MODE $it")
                mainActivityEventChannel.send(MainActivityEvents.ShowUpdateUI(it))
            }
        }
    }

    init {
        viewModelScope.launch {
            dataStoreManager.getData(DataStoreManager.PreferenceKeys.BASE_URL).collectLatest { url ->
                url?.let {
                    Timber.tag("baseurl").d("base : $url")
                    verificationRepository.setBaseUrl(it)
                }
            }

            dataStoreManager.getData(DataStoreManager.PreferenceKeys.SERVER_AVAILABILITY).collectLatest { serverName ->
                serverName?.let {
                    _serverAvailability.value = serverName
                }
            }
        }
    }

    fun startListenBroadcast() {
        isRunning.value = false
        application.registerReceiver(
            mMsgReceiver,
            IntentFilter(AppConfig.BROADCAST_ACTION_ACTIVITY)
        )
        MessageUtil.sendMsg2Service(getApplication(), AppConfig.MSG_REGISTER_CLIENT, "")
    }

    override fun onCleared() {
        application.unregisterReceiver(mMsgReceiver)
        tcpingTestScope.coroutineContext[Job]?.cancelChildren()
        SpeedtestUtil.closeAllTcpSockets()
        Timber.tag(ANG_PACKAGE).i("Main ViewModel is cleared")
        super.onCleared()
    }

    fun reloadServerList() {
        serverList = MmkvManager.decodeServerList()
        Timber.tag("ServersList").d("serverlist size : %s", serverList.size)

        updateCache()
        updateListAction.value = -1
        AngConfigManager.mainStorage?.encode(
            MmkvManager.KEY_SELECTED_SERVER,
            serversCache.lastOrNull()?.guid
        )


//        _serverAvailability.value =
//            MmkvManager.decodeServerConfig(serversCache.lastOrNull()?.guid ?: "")?.remarks
//                ?: "No server!"

    }

    fun removeServer(guid: String) {
        Timber.tag("MainViewModel").d(guid)
        serverList.remove(guid)
        MmkvManager.removeServer(guid)
        val index = getPosition(guid)
        if (index >= 0) {
            serversCache.removeAt(index)
        }
    }

    fun appendCustomConfigServer(server: String) {
        val config = ServerConfig.create(EConfigType.CUSTOM)
        config.remarks = System.currentTimeMillis().toString()
        config.subscriptionId = subscriptionId
        config.fullConfig = Gson().fromJson(server, V2rayConfig::class.java)
        val key = MmkvManager.encodeServerConfig("", config)
        serverRawStorage?.encode(key, server)
        serverList.add(key)
        serversCache.add(ServersCache(key, config))
    }

    fun swapServer(fromPosition: Int, toPosition: Int) {
        Collections.swap(serverList, fromPosition, toPosition)
        Collections.swap(serversCache, fromPosition, toPosition)
        mainStorage?.encode(KEY_ANG_CONFIGS, Gson().toJson(serverList))
    }

    @Synchronized
    fun updateCache() {
        serversCache.clear()
        for (guid in serverList) {
            val config = MmkvManager.decodeServerConfig(guid) ?: continue
            if (subscriptionId.isNotEmpty() && subscriptionId != config.subscriptionId) {
                Timber.tag("ServersList")
                    .d("subscriptionId.isNotEmpty() && subscriptionId != config.subscriptionId")
                continue
            }

            if (keywordFilter.isEmpty() || config.remarks.contains(keywordFilter)) {
                Timber.tag("ServersList")
                    .d("keywordFilter.isEmpty() || config.remarks.contains(keywordFilter")
                serversCache.add(ServersCache(guid, config))
            }
        }
    }

    fun testCurrentServerRealPing() {
        MessageUtil.sendMsg2Service(getApplication(), AppConfig.MSG_MEASURE_DELAY, "")
    }

    fun getPosition(guid: String): Int {
        serversCache.forEachIndexed { index, it ->
            if (it.guid == guid)
                return index
        }
        return -1
    }

    fun onActiveVpnClicked(context: Context) {
//        var androidId = Settings.Secure.getString(context.contentResolver,
//            Settings.Secure.ANDROID_ID);
        val enterVoucherBottomSheetDialog = EnterVoucherBottomSheetDialog()
        enterVoucherBottomSheetDialog.show(
            (context as MainActivity).supportFragmentManager,
            "voucher"
        )

//        var publicKey = KeyManage().getPublic()
//        setToClipBoard(context, publicKey)
//
//        context.toast("Device Id Copied to Clipboard")

    }

    fun onServersClicked(context: Context) {

        val serverListBottomSheetDialog = ServerListBottomSheetDialog()
        serverListBottomSheetDialog.show(
            (context as MainActivity).supportFragmentManager,
            "serverList"
        )

//        var publicKey = KeyManage().getPublic()
//        setToClipBoard(context, publicKey)
//
//        context.toast("Device Id Copied to Clipboard")

    }

    private fun setToClipBoard(context: Context, text: String) {
        val clipboard =
            ContextCompat.getSystemService(context, ClipboardManager::class.java)
        val clip = ClipData.newPlainText(null, text)
        clipboard!!.setPrimaryClip(clip)
    }


    private val mMsgReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.getIntExtra("key", 0)) {
                AppConfig.MSG_STATE_RUNNING -> {
                    isRunning.value = true
                }
                AppConfig.MSG_STATE_NOT_RUNNING -> {
                    isRunning.value = false
                }
                AppConfig.MSG_STATE_START_SUCCESS -> {
                    application.toast(R.string.toast_services_success)
                    isRunning.value = true
                }
                AppConfig.MSG_STATE_START_FAILURE -> {
                    application.toast(R.string.toast_services_failure)
                    isRunning.value = false
                }
                AppConfig.MSG_STATE_STOP_SUCCESS -> {
                    isRunning.value = false
                }
                AppConfig.MSG_MEASURE_DELAY_SUCCESS -> {
                    updateTestResultAction.value = intent.getStringExtra("content")
                }
                AppConfig.MSG_MEASURE_CONFIG_SUCCESS -> {
                    val resultPair = intent.getSerializableExtra("content") as Pair<String, Long>
                    MmkvManager.encodeServerTestDelayMillis(resultPair.first, resultPair.second)
                    updateListAction.value = getPosition(resultPair.first)
                }
            }
        }
    }

    suspend fun importBatchConfig(server: String?, subside: String = "", context: Context) {
        Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("server : $server")
        val subside2 = if (subside.isNullOrEmpty()) {
            subscriptionId
        } else {
            subside
        }
        val append = subside.isNullOrEmpty()

        var count = AngConfigManager.importBatchConfig(server, subside2, append)
        if (count <= 0) {
            AngConfigManager.importBatchConfig(Utils.decode(server!!), subside2, append)
            context.toastLong(R.string.wrong_config_2)
            setAppActivated(false)
        } else {
            if (serverList.size >= 1) {
                // Delete servers
                while (serversCache.size > 0) {
                    removeServer(serversCache[serversCache.size - 1].guid)
                }
                AngConfigManager.importBatchConfig(server, subside2, append)
                (context as MainActivity).defaultSharedPreferences.edit()
                    .putString(AppConfig.LAST_SERVER, "")
                    .apply()

            } else {
                (context as MainActivity).defaultSharedPreferences.edit()
                    .putString(AppConfig.LAST_SERVER, "")
                    .apply()
            }

            reloadServerList()
            V2RayServiceManager.startV2Ray(context)

        }
        _serverAvailability.value =
            MmkvManager.decodeServerConfig(serversCache.lastOrNull()?.guid ?: "")?.remarks
                ?: "No server!"

        dataStoreManager.updateData(DataStoreManager.PreferenceKeys.SERVER_AVAILABILITY,
            MmkvManager.decodeServerConfig(serversCache.lastOrNull()?.guid ?: "")?.remarks
                ?: "No server!")

        viewModelScope.launch {
            mainActivityEventChannel.send(MainActivityEvents.HideCircle)
        }

    }

    fun disconnectApi() = viewModelScope.launch {
//        Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("disconnectApi1")
        config.value = ""
        val token = dataStoreManager.getData(ACCESS_TOKEN).first()
        if (token != null) {
            try {
//                    Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("disconnectApi2")
                val publicS = dataStoreManager.getData(PUBLIC_S).first()
                val tokenE = KeyManage.instance.getToken(
                    token,
                    publicS ?: ""
                )
//                    Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("disconnectApi3")
                verificationRepository.disconnect(tokenE).collectLatest {
//                        Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("disconnectApi4")
                    dataStoreManager.updateData(IS_CONNECTED, false);
                    mainActivityEventChannel.send(MainActivityEvents.Disconnected(""))
                }
            } catch (e: Exception) {
                mainActivityEventChannel.send(MainActivityEvents.Disconnected(""))
            }
        }
    }

    fun listenToken() = viewModelScope.launch {
        Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("listenToken")
        val token = dataStoreManager.getData(ACCESS_TOKEN).first()
        if (token != null) {
            try {
                val publicS = dataStoreManager.getData(PUBLIC_S).first()
                val tokenE = KeyManage.instance.getToken(
                    token,
                    publicS ?: ""
                )
                val sN = dataStoreManager.getData(SERVER_ID).first() ?: 0

                getConfig(tokenE, sN)
                getTime()
            } catch (e: Exception) {
                setAppActivated(false)
                mainActivityEventChannel.send(MainActivityEvents.GetConfigMessage("Activate Again"))
            }
        } else {
            setAppActivated(false)
        }

    }

    fun getConfig(token: String, serverNumber : Int) = viewModelScope.launch(Dispatchers.IO) {
        Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("getConfig")
        verificationRepository.getConfig(
            token,
            serverNumber
        ).collectLatest { res ->
            when (res) {
                is Result.Error -> {
                    Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d(res.message)
                    mainActivityEventChannel.send(MainActivityEvents.GetConfigMessage(res.message))
                    base_url_counter.value++
                    Timber.tag("baseurl").d("base_url_counter ${base_url_counter.value}")
                }
                is Result.Loading -> {
                }
                is Result.Success -> {
                    base_url_counter.value = 0
                    checkVersionCode(res)
                }
            }
        }
    }

    private suspend fun checkVersionCode(res: Result<ConfigResponse>) {
        when (res.data?.status?.code) {
            0 -> {
                if (res.data.lastVersion > BuildConfig.VERSION_CODE) {
                    showUpdateUI(true)
                    getUpdateLink(res.data.config)
                } else {
                    dataStoreManager.updateData(
                        DataStoreManager.PreferenceKeys.IS_UPDATE_MODE,
                        false
                    )
                    config.value = res.data.config
                    setAppActivated(true)
                    dataStoreManager.updateData(IS_CONNECTED, true)
                    mainActivityEventChannel.send(MainActivityEvents.ShowMessageDialog(res.data.status.message))
                }
            }
            -1 -> {
                mainActivityEventChannel.send(MainActivityEvents.GetConfigMessage(res.data.status.message))
            }
            -2 -> {
                // deactivate app
                mainActivityEventChannel.send(
                    MainActivityEvents.GetConfigMessage(
                        "You have Logged Out.Please Login Again."
                    )
                )
                dataStoreManager.clearDataStore()
            }
            -3 -> {
                mainActivityEventChannel.send(MainActivityEvents.GetConfigMessage(res.data.status.message))
            }
            -7 -> {
                // active tunnel problem
                mainActivityEventChannel.send(MainActivityEvents.GetConfigMessage("Technical Problem.Please Contact Support"))
            }
            else -> {
                mainActivityEventChannel.send(MainActivityEvents.GetConfigMessage(res.data?.status?.message))
            }
        }
    }

    fun showUpdateUI(isUpdate: Boolean) = viewModelScope.launch {
        dataStoreManager.updateData(DataStoreManager.PreferenceKeys.IS_UPDATE_MODE, isUpdate)
        mainActivityEventChannel.send(MainActivityEvents.ShowUpdateUI(isUpdate))
        Timber.tag("showUpdateUI").d("showUpdateUI it $isUpdate")
    }

    private fun getUpdateLink(newConfig: String?) = viewModelScope.launch(Dispatchers.IO) {
        val token = dataStoreManager.getData(ACCESS_TOKEN).first()
        Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("getUpdateLink $token")
        if (token != null) {
            try {
                Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("getUpdateLink")
                val publicS = dataStoreManager.getData(PUBLIC_S).first()
                val tokenE = KeyManage.instance.getToken(
                    token,
                    publicS ?: ""
                )
                verificationRepository.getUpdateLink(
                    UpdateLinkRequest(
                        tokenE,
                        BuildConfig.VERSION_CODE
                    )
                ).collectLatest { res ->
                    when (res.data?.status?.code) {
                        0 -> {
                            if (res.data.required == 1) {
                                isUpdateRequired.value = true
                                mainActivityEventChannel.send(MainActivityEvents.HideCircle)
                            } else {
                                isUpdateRequired.value = false
                                if (newConfig != null) {
                                    config.value = newConfig
                                    setAppActivated(true)
                                    dataStoreManager.updateData(IS_CONNECTED, true);
                                }
                            }
                            dataStoreManager.updateData(
                                DataStoreManager.PreferenceKeys.UPP_LLIINK,
                                res.data.link
                            )
                            if (newConfig == null) downloadAppFileRecursive(application.applicationContext)
                        }
                        else -> {
                            if (res.data != null) {
                                if (newConfig != null) {
                                    config.value = newConfig
                                    setAppActivated(true)
                                    dataStoreManager.updateData(IS_CONNECTED, true)
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }


    fun setAppActivated(status: Boolean) = viewModelScope.launch {
        mainActivityEventChannel.send(MainActivityEvents.ActivateApp(status))
        if (!status) {
            while (serversCache.size > 0) {
                removeServer(serversCache[serversCache.size - 1].guid)
            }
        }
    }

    private fun checkAppActivated() = viewModelScope.launch {
        dataStoreManager.getData(ACCESS_TOKEN).collectLatest { token ->
            Timber.d("appstatus token $token")
            isAppActive = token != null
            setAppActivated(token != null)
        }
    }

    private fun getTime() = viewModelScope.launch {
        verificationRepository.getTime().collectLatest { res ->
            res.data?.unix?.en.let { en ->
                Timber.d("enn $en")
                val unixTime = System.currentTimeMillis()
                var serverUnix = en?.times(1000L)
                val acceptableDiffrence = 600000L
                if(Math.abs(unixTime - (serverUnix ?: unixTime)) > acceptableDiffrence)
                    mainActivityEventChannel.send(MainActivityEvents.ShowTimeDialog)

            }
        }
    }


    fun onLogoutClicked() = viewModelScope.launch {
        mainActivityEventChannel.send(MainActivityEvents.ShowLogoutDialog)
    }

    fun disconnectAndLogout(context: Context) {
        Utils.stopVService(context)
        disconnectApi()
        logout()
    }

    private fun logout() = viewModelScope.launch {
        val token = dataStoreManager.getData(ACCESS_TOKEN).first()
        if (token != null) {
            try {
                Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("onLogoutClicked")
                val publicS = dataStoreManager.getData(PUBLIC_S).first()
                val tokenE = KeyManage.instance.getToken(
                    token,
                    publicS ?: ""
                )
                Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("onLogoutClicked2")
                verificationRepository.logout(tokenE).collectLatest { res ->
                    when (res) {
                        is Result.Error -> {
                            mainActivityEventChannel.send(MainActivityEvents.ShowMessage("Couldn't Logout. Check your internet connection"))
                        }
                        is Result.Loading -> {
                            mainActivityEventChannel.send(MainActivityEvents.ShowMessage("Logging out ..."))
                        }
                        is Result.Success -> {
                            when (res.data?.status?.code) {
                                0 -> {
                                    mainActivityEventChannel.send(MainActivityEvents.ShowMessage("You Are Logged Out"))
                                    mainActivityEventChannel.send(MainActivityEvents.HideCircle)
                                    dataStoreManager.clearDataStore()

                                    setAppActivated(false)

                                }
                                -1 -> {
                                    // wrong token ke karbar nemikhorad
                                    mainActivityEventChannel.send(MainActivityEvents.ShowMessage(res.data.status.message))
                                }
                                -2 -> {
                                    //
                                    mainActivityEventChannel.send(MainActivityEvents.ShowMessage(res.data.status.message))
                                }
                                -4 -> {
                                    // max reset
                                    mainActivityEventChannel.send(MainActivityEvents.MaxLoginDialog)
                                }
                                else -> {
                                    //
                                    mainActivityEventChannel.send(
                                        MainActivityEvents.ShowMessage(
                                            "Error code ${res.data?.status?.code}"
                                        )
                                    )
                                }
                            }
                        }
                    }
                    Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("onLogoutClicked3")

                }
            } catch (e: Exception) {
            }
        }
    }

    //control request count
    private var counter_401 = 0

    fun downloadAPKFromServer(context: Context?) = viewModelScope.launch(Dispatchers.IO) {
        // Create a URL object from the download URL(
        config.value = ""
        context?.let { Utils.stopVService(it) }
        var statusCode = 0
        try {
            val link =
                dataStoreManager.getData(DataStoreManager.PreferenceKeys.UPP_LLIINK).firstOrNull()
            val url = URL(link)

            // Open a connection to the server
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            Timber.tag("downloads").d("link $link")
            statusCode = connection.responseCode
            Timber.tag("downloadss").d("statusCode $statusCode")

            // Set up the connection parameters
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            // Get the filename from the Content-Disposition header
            val contentDisposition = connection.getHeaderField("Content-Disposition")
            if (contentDisposition != null) {
                val index = contentDisposition.indexOf("filename=")
                if (index > 0) {
                    appFileName = contentDisposition.substring(index + 9, contentDisposition.length)
                        .replace("\"", "")
                }
            }
            // Get the input stream from the connection
            val inputStream = connection.inputStream
            // Create a FileOutputStream to save the downloaded APK
            val apkFile = File(application.getExternalFilesDir(null), appFileName)
            Timber.tag("downloads").d("appFileName $appFileName")
            val outputStream = FileOutputStream(apkFile)
            savedStateHandle["downloading"] = DownloadAppStatus.STARTED
            mainActivityEventChannel.send(MainActivityEvents.DownloadStarted)
            // Buffer to read data in chunks
            val buffer = ByteArray(1024)
            var bytesRead: Int
            var totalBytesRead: Long = 0
            val totalFileSize = connection.contentLength.toLong()
            // Download the APK and save it to the file
            var progress = 0
            while (inputStream.read(buffer).also { bytesRead = it } > 0) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                progress = (totalBytesRead * 100 / totalFileSize).toInt()
                _downloadPercentage.value = progress
            }

            // Close the streams
            outputStream.close()
            inputStream.close()
            savedStateHandle["downloading"] = DownloadAppStatus.FINISHED
            mainActivityEventChannel.send(MainActivityEvents.DownloadFinished(progress, apkFile))
        } catch (e: Exception) {
            Timber.tag("downloads").d("e ${e.message}")
            if (statusCode == 401 && counter_401 < 2) {
                getUpdateLink(newConfig = null)
                counter_401++
            } else {
                counter_401 = 0
                savedStateHandle["downloading"] = DownloadAppStatus.FAILED
                mainActivityEventChannel.send(MainActivityEvents.DownloadFailed)
            }
        }
    }

    private fun downloadAppFileRecursive(context: Context?) {
        downloadAPKFromServer(context)
    }

    fun getBaseAddress() = viewModelScope.launch {
        verificationRepository.getBaseAddress().collectLatest { res ->
            when (res.data?.status?.code) {
                0 -> {
//                    ApiUrl.BASE_URL = res.data.link
                    ApiUrl.base_url_counter.value = 0
                    dataStoreManager.updateData(DataStoreManager.PreferenceKeys.BASE_URL, res.data.link)
                    Timber.d("getBaseurl succeed")
                }
                else -> {
                    Timber.d("getBaseurl failed")
                }
            }
        }

    }

    suspend fun copyToClipboard(context: Context) {
            dataStoreManager.getData(DataStoreManager.PreferenceKeys.CODE).collectLatest {
                if(!it.isNullOrEmpty()) {
                    Utils.copyToClipboard(
                        context = context,
                        text = it
                    )
                    context.toastLong("کد شما کپی شد. می توانید آن را پیست کنید")
                } else if(isAppActive) {
                    context.toast("Login Again to enable this feature")
                } else
                    context.toast("Login First")
            }

    }

    fun updateServerName() = viewModelScope.launch{
        Timber.tag("QSTILE").d("updateServerName")
        dataStoreManager.getData(DataStoreManager.PreferenceKeys.SERVER_AVAILABILITY).collectLatest { serverName ->
            Timber.tag("QSTILE").d("servername    $serverName")
            serverName?.let {
                _serverAvailability.value = it
            }
        }
    }

}
