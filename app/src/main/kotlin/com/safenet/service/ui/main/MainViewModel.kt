package com.safenet.service.ui.main

import android.content.*
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.safenet.service.AngApplication
import com.safenet.service.AppConfig
import com.safenet.service.AppConfig.ANG_PACKAGE
import com.safenet.service.R
import com.safenet.service.data.local.DataStoreManager
import com.safenet.service.data.local.DataStoreManager.PreferenceKeys.ACCESS_TOKEN
import com.safenet.service.data.local.DataStoreManager.PreferenceKeys.PUBLIC_S
import com.safenet.service.data.repository.VerificationRepository
import com.safenet.service.dto.*
import com.safenet.service.extension.toast
import com.safenet.service.extension.toastLong
import com.safenet.service.ui.voucher_bottomsheet.EnterVoucherBottomSheetDialog
import com.safenet.service.ui.voucher_bottomsheet.EnterVoucherBottomSheetViewModel
import com.safenet.service.util.*
import com.safenet.service.util.MmkvManager.KEY_ANG_CONFIGS
import com.tencent.mmkv.MMKV
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import com.safenet.service.data.network.Result
import com.safenet.service.service.V2RayServiceManager
import com.safenet.service.ui.MainActivity
import kotlinx.coroutines.channels.Channel


@HiltViewModel
class MainViewModel @Inject constructor(
    private val verificationRepository: VerificationRepository,
    val application: AngApplication,
    private val dataStoreManager: DataStoreManager
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

    private val _serverAvailability = MutableStateFlow<String?>(null)
    val serverAvailability: StateFlow<String?> get() = _serverAvailability

    var config = MutableStateFlow("")
        private set

    var isAppActivated = MutableStateFlow(false)
        private set

    private val mainActivityEventChannel = Channel<MainActivityEvents>()
    val mainActivityEvent = mainActivityEventChannel.receiveAsFlow()


    private val tcpingTestScope by lazy { CoroutineScope(Dispatchers.IO) }

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
        Log.i(ANG_PACKAGE, "Main ViewModel is cleared")
        super.onCleared()
    }

    fun reloadServerList() {
        serverList = MmkvManager.decodeServerList()
        Log.d("ServersList", "serverlist size : ${serverList.size}")

        updateCache()
        updateListAction.value = -1
        AngConfigManager.mainStorage?.encode(
            MmkvManager.KEY_SELECTED_SERVER,
            serversCache.lastOrNull()?.guid
        )


        _serverAvailability.value =
            MmkvManager.decodeServerConfig(serversCache.lastOrNull()?.guid ?: "")?.remarks
                ?: "No server!"

    }

    fun removeServer(guid: String) {
        Log.d("MainViewModel", guid)
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

//    fun testAllTcping() {
//        tcpingTestScope.coroutineContext[Job]?.cancelChildren()
//        SpeedtestUtil.closeAllTcpSockets()
//        MmkvManager.clearAllTestDelayResults()
//        updateListAction.value = -1 // update all
//
//        application.toast(R.string.connection_test_testing)
//        for (item in serversCache) {
//            item.config.getProxyOutbound()?.let { outbound ->
//                val serverAddress = outbound.getServerAddress()
//                val serverPort = outbound.getServerPort()
//                if (serverAddress != null && serverPort != null) {
//                    tcpingTestScope.launch {
//                        val testResult = SpeedtestUtil.tcping(serverAddress, serverPort)
//                        launch(Dispatchers.Main) {
//                            MmkvManager.encodeServerTestDelayMillis(item.guid, testResult)
//                            updateListAction.value = getPosition(item.guid)
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    fun testAllRealPing() {
//        MessageUtil.sendMsg2TestService(getApplication(), AppConfig.MSG_MEASURE_CONFIG_CANCEL, "")
//        MmkvManager.clearAllTestDelayResults()
//        updateListAction.value = -1 // update all
//
//        application.toast(R.string.connection_test_testing)
//        viewModelScope.launch(Dispatchers.Default) { // without Dispatchers.Default viewModelScope will launch in main thread
//            for (item in serversCache) {
//                val config = V2rayConfigUtil.getV2rayConfig(getApplication(), item.guid)
//                if (config.status) {
//                    MessageUtil.sendMsg2TestService(
//                        getApplication(),
//                        AppConfig.MSG_MEASURE_CONFIG,
//                        Pair(item.guid, config.content)
//                    )
//                }
//            }
//        }
//    }

    fun testCurrentServerRealPing() {
        MessageUtil.sendMsg2Service(getApplication(), AppConfig.MSG_MEASURE_DELAY, "")
    }

//    fun filterConfig(context: Context) {
//        val subscriptions = MmkvManager.decodeSubscriptions()
//        val listId = subscriptions.map { it.first }.toList().toMutableList()
//        val listRemarks = subscriptions.map { it.second.remarks }.toList().toMutableList()
//        listRemarks += context.getString(R.string.filter_config_all)
//        val checkedItem = if (subscriptionId.isNotEmpty()) {
//            listId.indexOf(subscriptionId)
//        } else {
//            listRemarks.count() - 1
//        }
//
//        val ivBinding = DialogConfigFilterBinding.inflate(LayoutInflater.from(context))
//        ivBinding.spSubscriptionId.adapter = ArrayAdapter<String>(
//            context,
//            android.R.layout.simple_spinner_dropdown_item,
//            listRemarks
//        )
//        ivBinding.spSubscriptionId.setSelection(checkedItem)
//        ivBinding.etKeyword.text = Utils.getEditable(keywordFilter)
//        val builder = AlertDialog.Builder(context).setView(ivBinding.root)
//        builder.setTitle(R.string.title_filter_config)
//        builder.setPositiveButton(R.string.tasker_setting_confirm) { dialogInterface: DialogInterface?, _: Int ->
//            try {
//                val position = ivBinding.spSubscriptionId.selectedItemPosition
//                subscriptionId = if (listRemarks.count() - 1 == position) {
//                    ""
//                } else {
//                    subscriptions[position].first
//                }
//                keywordFilter = ivBinding.etKeyword.text.toString()
//                reloadServerList()
//
//                dialogInterface?.dismiss()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//        builder.show()
////        AlertDialog.Builder(context)
////            .setSingleChoiceItems(listRemarks.toTypedArray(), checkedItem) { dialog, i ->
////                try {
////                    subscriptionId = if (listRemarks.count() - 1 == i) {
////                        ""
////                    } else {
////                        subscriptions[i].first
////                    }
////                    reloadServerList()
////                    dialog.dismiss()
////                } catch (e: Exception) {
////                    e.printStackTrace()
////                }
////            }.show()
//    }

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

    fun importBatchConfig(server: String?, subside: String = "", context: Context) {
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
            context.toastLong(R.string.wrong_confige)
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
    }

    fun disconnectApi() = viewModelScope.launch {
//        Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("disconnectApi1")
        dataStoreManager.getData(ACCESS_TOKEN).collectLatest { token ->
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
                        mainActivityEventChannel.send(MainActivityEvents.Disconnected(""))
                    }
                } catch (e: Exception) {
                    mainActivityEventChannel.send(MainActivityEvents.Disconnected(""))
                }
            }
        }
    }

    fun listenToken() = viewModelScope.launch {
        Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("listenToken")
        dataStoreManager.getData(ACCESS_TOKEN).collectLatest { token ->
            if (token != null) {
                try {
                    val publicS = dataStoreManager.getData(PUBLIC_S).first()
                    val tokenE = KeyManage.instance.getToken(
                        token,
                        publicS ?: ""
                    )
                    getConfig(tokenE)
                } catch (e: Exception) {
                    setAppActivated(false)
                    mainActivityEventChannel.send(MainActivityEvents.GetConfigMessage("Activate Again"))
                }
            }
            else {
                setAppActivated(false)
            }
        }
    }

    fun getConfig(token: String) = viewModelScope.launch(Dispatchers.IO) {
        verificationRepository.getConfig(
            token
        ).collectLatest { res ->
            when(res){
                is Result.Error -> {
                    Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d(res.message)
                    mainActivityEventChannel.send(MainActivityEvents.GetConfigMessage(res.message))
                }
                is Result.Loading ->{
                }
                is Result.Success -> {
                    when(res.data?.status?.code){
                        0 -> {
                            config.value = res.data.config
                            setAppActivated(true)
                        }
                        -1 -> {
                            mainActivityEventChannel.send(MainActivityEvents.GetConfigMessage(res.data.status.massage))
                        }
                        -2 -> {
                            mainActivityEventChannel.send(MainActivityEvents.GetConfigMessage(
                                "Your Voucher Time has been Expired!"
                            ))
                            dataStoreManager.clearDataStore()
                        }
                        -3 -> {
                            mainActivityEventChannel.send(MainActivityEvents.GetConfigMessage(res.data.status.massage))
                        }
                        else -> {
                            mainActivityEventChannel.send(MainActivityEvents.GetConfigMessage(res.data?.status?.massage))
                        }
                    }
                }
            }
            Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("getConfig")
        }
    }

    private fun setAppActivated(status : Boolean) = viewModelScope.launch {
        if (!status){
            while (serversCache.size > 0) {
                removeServer(serversCache[serversCache.size - 1].guid)
            }
        }

        mainActivityEventChannel.send(MainActivityEvents.ActivateApp(status))
    }

    fun checkAppActivated() = viewModelScope.launch {
        dataStoreManager.getData(ACCESS_TOKEN).collectLatest { token ->
            Timber.d("appstatus token $token")
            setAppActivated(token != null)
        }
    }
}
