package com.safenet.service.service

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.safenet.service.AngApplication
import com.safenet.service.AppConfig
import com.safenet.service.R
import com.safenet.service.data.local.DataStoreManager
import com.safenet.service.data.network.Result
import com.safenet.service.data.repository.VerificationRepository
import com.safenet.service.dto.ServersCache
import com.safenet.service.extension.toast
import com.safenet.service.ui.voucher_bottomsheet.EnterVoucherBottomSheetViewModel
import com.safenet.service.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.ref.SoftReference
import javax.inject.Inject

@TargetApi(Build.VERSION_CODES.N)
@AndroidEntryPoint
class QSTileService : TileService() {

    @Inject
    lateinit var application: AngApplication

    @Inject
    lateinit var verificationRepository: VerificationRepository

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    var serverList = MmkvManager.decodeServerList()
    val serversCache = mutableListOf<ServersCache>()


    fun setState(state: Int) {
        if (state == Tile.STATE_INACTIVE) {
            qsTile?.state = Tile.STATE_INACTIVE
            qsTile?.label = getString(R.string.app_name)
            qsTile?.icon = Icon.createWithResource(applicationContext, R.drawable.ic_stat_name)
        } else if (state == Tile.STATE_ACTIVE) {
            qsTile?.state = Tile.STATE_ACTIVE
            qsTile?.label = V2RayServiceManager.currentConfig?.remarks
            qsTile?.icon = Icon.createWithResource(applicationContext, R.drawable.ic_stat_name)
        }

        qsTile?.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        setState(Tile.STATE_INACTIVE)
        mMsgReceive = ReceiveMessageHandler(this)
        registerReceiver(mMsgReceive, IntentFilter(AppConfig.BROADCAST_ACTION_ACTIVITY))
        MessageUtil.sendMsg2Service(this, AppConfig.MSG_REGISTER_CLIENT, "")
    }

    override fun onStopListening() {
        super.onStopListening()

        unregisterReceiver(mMsgReceive)
        mMsgReceive = null
    }

    override fun onClick() {
        super.onClick()
        when (qsTile.state) {
            Tile.STATE_INACTIVE -> {
                Timber.d("startService 3")
//                listenToken(this)
                toast("Will be enabled in next version")
            }
            Tile.STATE_ACTIVE -> {
//                disconnectApi()
                Utils.stopVService(this)
            }
        }
    }

    private var mMsgReceive: BroadcastReceiver? = null

    private class ReceiveMessageHandler(context: QSTileService) : BroadcastReceiver() {
        internal var mReference: SoftReference<QSTileService> = SoftReference(context)
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val context = mReference.get()
            when (intent?.getIntExtra("key", 0)) {
                AppConfig.MSG_STATE_RUNNING -> {
                    context?.setState(Tile.STATE_ACTIVE)
                }
                AppConfig.MSG_STATE_NOT_RUNNING -> {
                    context?.setState(Tile.STATE_INACTIVE)
                }
                AppConfig.MSG_STATE_START_SUCCESS -> {
                    context?.setState(Tile.STATE_ACTIVE)
                }
                AppConfig.MSG_STATE_START_FAILURE -> {
                    context?.setState(Tile.STATE_INACTIVE)
                }
                AppConfig.MSG_STATE_STOP_SUCCESS -> {
                    context?.setState(Tile.STATE_INACTIVE)
                }
            }
        }
    }

//    fun listenToken(context: Context) = scope.launch {
//        Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("listenToken")
//        dataStoreManager.getData(DataStoreManager.PreferenceKeys.ACCESS_TOKEN)
//            .collectLatest { token ->
//                if (token != null) {
//                    try {
//                        val publicS =
//                            dataStoreManager.getData(DataStoreManager.PreferenceKeys.PUBLIC_S)
//                                .first()
//                        val tokenE = KeyManage.instance.getToken(
//                            token,
//                            publicS ?: ""
//                        )
//                        getConfig(tokenE, context)
//                    } catch (e: Exception) {
////                    setAppActivated(false)
//                    }
//                } else {
////                setAppActivated(false)
//                }
//            }
//    }

    private fun getConfig(tokenE: String, context: Context) = scope.launch {
        verificationRepository.getConfig(
            tokenE,
            Utils.getOsInfo()
        ).collectLatest{
                res ->
            when(res){
                is Result.Error -> {
                    Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d(res.message)
                    context.toast("unsuccessful")
                }
                is Result.Loading ->{
                }
                is Result.Success -> {
                    when(res.data?.status?.code){
                        0 -> {
                            importBatchConfig(KeyManage.instance.getConfig(res?.data?.config), "",context)
                        }
                        else -> {
                            context.toast("unsuccessful")
                        }
                    }
                }
            }

        }
    }

    fun getPosition(guid: String): Int {
        serversCache.forEachIndexed { index, it ->
            if (it.guid == guid)
                return index
        }
        return -1
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

    fun importBatchConfig(server: String?, subside: String = "", context: Context) {
        Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("server : $server")
        val subside2 = if (subside.isNullOrEmpty()) {
            ""
        } else {
            subside
        }
        val append = subside.isNullOrEmpty()

        var count = AngConfigManager.importBatchConfig(server, subside2, append)
        if (count <= 0) {
            AngConfigManager.importBatchConfig(Utils.decode(server!!), subside2, append)
        } else {
            if (serverList.size >= 1) {
                // Delete servers
//                while (serversCache.size > 0) {
//                    removeServer(serversCache[serversCache.size - 1].guid)
//                }
                AngConfigManager.importBatchConfig(server, subside2, append)
//                defaultSharedPreferences.edit()
//                    .putString(AppConfig.LAST_SERVER, "")
//                    .apply()

            } else {
//                defaultSharedPreferences.edit()
//                    .putString(AppConfig.LAST_SERVER, "")
//                    .apply()
            }

            reloadServerList()
//            V2RayServiceManager.startV2Ray(this)
            V2RayServiceManager.startV2Ray(context)

        }
//        _serverAvailability.value =
//            MmkvManager.decodeServerConfig(serversCache.lastOrNull()?.guid ?: "")?.remarks
//                ?: "No server!"
    }

    private fun reloadServerList() {
        serverList = MmkvManager.decodeServerList()
        Log.d("ServersList", "serverlist size : ${serverList.size}")

        serversCache.clear()

        AngConfigManager.mainStorage?.encode(
            MmkvManager.KEY_SELECTED_SERVER,
            serversCache.lastOrNull()?.guid
        )
    }

//    fun disconnectApi() = scope.launch {
////        Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("disconnectApi1")
//        dataStoreManager.getData(DataStoreManager.PreferenceKeys.ACCESS_TOKEN).collectLatest { token ->
//            if (token != null) {
//                try {
////                    Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("disconnectApi2")
//                    val publicS = dataStoreManager.getData(DataStoreManager.PreferenceKeys.PUBLIC_S).first()
//                    val tokenE = KeyManage.instance.getToken(
//                        token,
//                        publicS ?: ""
//                    )
////                    Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("disconnectApi3")
//                    verificationRepository.disconnect(tokenE).collectLatest {
//                        Utils.stopVService(this@QSTileService)
////                        Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("disconnectApi4")
//                    }
//                } catch (e: Exception) {
//                    Utils.stopVService(this@QSTileService)
//                }
//            } else {
//                Utils.stopVService(this@QSTileService)
//            }
//        }
//    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}

