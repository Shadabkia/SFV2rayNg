package com.safenet.service.ui.main

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.net.VpnService
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.safenet.service.AppConfig
import com.safenet.service.AppConfig.ANG_PACKAGE
import com.safenet.service.R
import com.safenet.service.databinding.ActivityMainBinding
import com.safenet.service.extension.toast
import com.safenet.service.extension.toastLong
import com.safenet.service.helper.SimpleItemTouchHelperCallback
import com.safenet.service.ui.BaseActivity
import com.safenet.service.ui.MainRecyclerAdapter
import com.safenet.service.ui.ServerActivity
import com.safenet.service.ui.voucher_bottomsheet.EnterVoucherBottomSheetViewModel
import com.safenet.service.util.AngConfigManager
import com.safenet.service.util.KeyManage
import com.safenet.service.util.MmkvManager
import com.safenet.service.util.Utils
import com.tbruyelle.rxpermissions.RxPermissions
import com.tencent.mmkv.MMKV
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.drakeet.support.toast.ToastCompat
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    private val adapter by lazy { MainRecyclerAdapter(this) }
    private val mainStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_MAIN,
            MMKV.MULTI_PROCESS_MODE
        )
    }
    private val settingsStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_SETTING,
            MMKV.MULTI_PROCESS_MODE
        )
    }
    private val requestVpnPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
//                startV2Ray()
            }
            binding.fab.isEnabled = true
        }
    private var mItemTouchHelper: ItemTouchHelper? = null
    val mainViewModel: MainViewModel by viewModels()

    val defaultSharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this@MainActivity) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        title = ""
        setRoutingRules()

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        val callback = SimpleItemTouchHelperCallback(adapter)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper?.attachToRecyclerView(binding.recyclerView)

        setupViewModel()
        copyAssets()
        migrateLegacy()
        activeRouting()


        this.lifecycleScope.launch {
            mainViewModel.mainActivityEvent.collectLatest { event ->
                when (event) {
                    is MainActivityEvents.ActivateApp -> activateApp(event.status)
                    is MainActivityEvents.GetConfigMessage -> {
                        hideCircle()
                        binding.fab.isEnabled = true
                        toastLong(event.message ?: "Error")
                    }
                    is MainActivityEvents.Disconnected -> {
                        binding.fab.isEnabled = true
                        Utils.stopVService(this@MainActivity)
                        hideCircle()
                    }
                    MainActivityEvents.ShowLogoutDialog -> showLogoutDialog()
                    is MainActivityEvents.ShowMessage -> toast(event.message)
                    MainActivityEvents.MaxLoginDialog -> showMaxLoginDialog()
                }

            }
        }

        listeners()
        initView()
    }

    private fun showMaxLoginDialog() {
        val dialog =  AlertDialog.Builder(this)
        dialog.setMessage(R.string.max_logout_message)
            .setPositiveButton(android.R.string.ok) { _,_ ->

            }
            .show()
    }

    private fun showLogoutDialog() {
        val dialog =  AlertDialog.Builder(this)
        dialog.setMessage(R.string.logout_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                mainViewModel.disconnectAndLogout()
            }
            .setNegativeButton(R.string.cancel){_,_ ->

            }
            .show()
    }

    private fun activateApp(status: Boolean) {
        Timber.d("appstatus $status")
        binding.apply {
            if (status) {
                navToolbar.activeVpn.text = getString(R.string.logout)
                navToolbar.activeVpn.setTextColor(
                    ContextCompat.getColor(
                        this@MainActivity,
                        R.color.colorPingRed
                    )
                )
                binding.fab.isEnabled = true
            } else {
                navToolbar.activeVpn.text = getString(R.string.login)
                navToolbar.activeVpn.setTextColor(
                    ContextCompat.getColor(
                        this@MainActivity,
                        R.color.colorSelected
                    )
                )
//                binding.serverAvailability.text = getString(R.string.no_server)
                binding.fab.isEnabled = false
            }
        }
    }

    private fun initView() {
        mainViewModel.checkAppActivated()
        this.lifecycleScope.launch {
            mainViewModel.config.collectLatest {
//                Timber.tag("ConfigApi").d("config : $it")
                if (it.isNotEmpty()) {
                    importClipboard(it)
                }
                hideCircle()
                binding.fab.isEnabled = true
            }
        }
    }

    private fun listeners() {
        binding.apply {
            navToolbar.apply {
                activeVpn.setOnClickListener {
                    if ((it as MaterialButton).text == getString(R.string.logout)) {
                        mainViewModel.onLogoutClicked()
                    } else
                        mainViewModel.onActiveVpnClicked(this@MainActivity)
                }
            }

            contactUs.setOnClickListener {
                val intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/safenet_vpn_admin"))
                startActivity(intent)
            }

            this@MainActivity.lifecycleScope.launch {
                mainViewModel.serverAvailability.collect {
                    serverAvailability.text = it
                }

            }
            fab.setOnClickListener { view ->
                this@MainActivity.lifecycleScope.launch {

                    if (mainViewModel.isRunning.value == true) {
                        mainViewModel.disconnectApi()
                    } else if ((settingsStorage?.decodeString(AppConfig.PREF_MODE)
                            ?: "VPN") == "VPN"
                    ) {
                        val intent = VpnService.prepare(this@MainActivity)
                        if (intent == null) {
                            if (fab.isEnabled) {
                                mainViewModel.listenToken()
                                showCircle()
                            }
                            view.isEnabled = false
                        } else {
                            requestVpnPermission.launch(intent)
                        }
                    } else {
                        if (fab.isEnabled) {
                            mainViewModel.listenToken()
                            showCircle()
                        }
                        view.isEnabled = false
                    }
                }
            }

            layoutTest.setOnClickListener {
                if (mainViewModel.isRunning.value == true) {
                    setTestState(getString(R.string.connection_test_testing))
                    mainViewModel.testCurrentServerRealPing()
                } else {
//                tv_test_state.text = getString(R.string.connection_test_fail)
                }
            }

        }
    }

    private fun setRoutingRules() {
        defaultSharedPreferences.edit()
            .putString(AppConfig.PREF_V2RAY_ROUTING_BLOCKED, getString(R.string.blocked_url_or_ip))
            .apply()
        defaultSharedPreferences.edit()
            .putString(AppConfig.PREF_V2RAY_ROUTING_DIRECT, getString(R.string.direct_url_or_ip))
            .apply()
    }

    private fun activeRouting() {
        // add routing
        settingsStorage?.encode(
            AppConfig.PREF_V2RAY_ROUTING_DIRECT,
            defaultSharedPreferences.getString(AppConfig.PREF_V2RAY_ROUTING_DIRECT, "")
        )
        settingsStorage?.encode(
            AppConfig.PREF_V2RAY_ROUTING_BLOCKED,
            defaultSharedPreferences.getString(AppConfig.PREF_V2RAY_ROUTING_BLOCKED, "")
        )

    }

    private fun setupViewModel() {
        mainViewModel.updateListAction.observe(this) { index ->
            if (index >= 0) {
                adapter.notifyItemChanged(index)
            } else {
                adapter.notifyDataSetChanged()
            }
        }
        mainViewModel.updateTestResultAction.observe(this) { setTestState(it) }
        mainViewModel.isRunning.observe(this) { isRunning ->
            adapter.isRunning = isRunning
            if (isRunning) {
                binding.fab.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorSelected))
                setTestState(getString(R.string.connection_connected))
                binding.layoutTest.isFocusable = true
                binding.fabText.text = getString(R.string.connected)
                binding.serverAvailability.text =
                    getString(R.string.server_name, mainViewModel.serverAvailability.value)
            } else {
                binding.fab.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorUnselected))
                setTestState(getString(R.string.connection_not_connected))
                binding.layoutTest.isFocusable = false
                binding.fabText.text = getString(R.string.connect)
                binding.serverAvailability.text = mainViewModel.serverAvailability.value
            }
        }
        mainViewModel.startListenBroadcast()
    }

    private fun copyAssets() {
        val extFolder = Utils.userAssetPath(this)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geo = arrayOf("geosite.dat", "geoip.dat", "iran.dat")
                assets.list("")
                    ?.filter { geo.contains(it) }
                    ?.filter { !File(extFolder, it).exists() }
                    ?.forEach {
                        val target = File(extFolder, it)
                        assets.open(it).use { input ->
                            FileOutputStream(target).use { output ->
                                input.copyTo(output)
                            }
                        }
                        Timber.tag(ANG_PACKAGE)
                            .i("Copied from apk assets folder to " + target.absolutePath)
                    }
            } catch (e: Exception) {
                Timber.tag(ANG_PACKAGE).e(e, "asset copy failed")
            }
        }
    }

    private fun migrateLegacy() {
        lifecycleScope.launch(Dispatchers.IO) {
            val result = AngConfigManager.migrateLegacyConfig(this@MainActivity)
            if (result != null) {
                launch(Dispatchers.Main) {
                    if (result) {
                        toast(getString(R.string.migration_success))
                        mainViewModel.reloadServerList()
                    } else {
                        toast(getString(R.string.migration_fail))
                    }
                }
            }
        }
    }

//    fun startV2Ray() {
//        if (mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER).isNullOrEmpty()) {
//            return
//        }
//        showCircle()
////        toast(R.string.toast_services_start)
//    }

    fun restartV2Ray() {
        if (mainViewModel.isRunning.value == true) {
            Utils.stopVService(this)
        }
        Observable.timer(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
//                startV2Ray()
            }
    }

    public override fun onResume() {
        super.onResume()
        mainViewModel.reloadServerList()
    }

    public override fun onPause() {
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    private fun importManually(createConfigType: Int) {
        startActivity(
            Intent()
                .putExtra("createConfigType", createConfigType)
                .putExtra("subscriptionId", mainViewModel.subscriptionId)
                .setClass(this, ServerActivity::class.java)
        )
    }

    /**
     * import config from qrcode
     */
    /**
     * import config from clipboard
     */
    private fun importClipboard(config: String)
            : Boolean {
        try {
//            val clipboard = Utils.getClipboard(this)
            val deConfig = KeyManage.instance.getConfig(config)
            Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("config : $deConfig")
            mainViewModel.importBatchConfig(deConfig, "", this@MainActivity)
        } catch (e: Exception) {
            e.printStackTrace()
            toastLong(R.string.wrong_config)
            return false
        }
        return true
    }


    fun importConfigCustomClipboard()
            : Boolean {
        try {
            val configText = Utils.getClipboard(this)
            if (TextUtils.isEmpty(configText)) {
                toast(R.string.toast_none_data_clipboard)
                return false
            }
            importCustomizeConfig(configText)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * import config from local config file
     */
    fun importConfigCustomLocal(): Boolean {
        try {
            showFileChooser()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun importConfigCustomUrlClipboard()
            : Boolean {
        try {
            val url = Utils.getClipboard(this)
            if (TextUtils.isEmpty(url)) {
                toast(R.string.toast_none_data_clipboard)
                return false
            }
            return importConfigCustomUrl(url)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * import config from url
     */
    private fun importConfigCustomUrl(url: String?): Boolean {
        try {
            if (!Utils.isValidUrl(url)) {
                toast(R.string.toast_invalid_url)
                return false
            }
            lifecycleScope.launch(Dispatchers.IO) {
                val configText = try {
                    Utils.getUrlContentWithCustomUserAgent(url)
                } catch (e: Exception) {
                    e.printStackTrace()
                    ""
                }
                launch(Dispatchers.Main) {
                    importCustomizeConfig(configText)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * import config from sub
     */
//    fun importConfigViaSub()
//            : Boolean {
//        try {
//            toast(R.string.title_sub_update)
//            MmkvManager.decodeSubscriptions().forEach {
//                if (TextUtils.isEmpty(it.first)
//                    || TextUtils.isEmpty(it.second.remarks)
//                    || TextUtils.isEmpty(it.second.url)
//                ) {
//                    return@forEach
//                }
//                if (!it.second.enabled) {
//                    return@forEach
//                }
//                val url = it.second.url
//                if (!Utils.isValidUrl(url)) {
//                    return@forEach
//                }
//                Log.d(ANG_PACKAGE, url)
//                lifecycleScope.launch(Dispatchers.IO) {
//                    val configText = try {
//                        Utils.getUrlContentWithCustomUserAgent(url)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        launch(Dispatchers.Main) {
//                            toast("\"" + it.second.remarks + "\" " + getString(R.string.toast_failure))
//                        }
//                        return@launch
//                    }
//                    launch(Dispatchers.Main) {
//                        mainViewModel.importBatchConfig(configText, it.first, this@MainActivity)
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return false
//        }
//        return true
//    }

    /**
     * show file chooser
     */
    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        try {
            chooseFileForCustomConfig.launch(
                Intent.createChooser(
                    intent,
                    getString(R.string.title_file_chooser)
                )
            )
        } catch (ex: ActivityNotFoundException) {
            toast(R.string.toast_require_file_manager)
        }
    }

    private val chooseFileForCustomConfig =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            if (it.resultCode == RESULT_OK && uri != null) {
                readContentFromUri(uri)
            }
        }

    /**
     * read content from uri
     */
    private fun readContentFromUri(uri: Uri) {
        RxPermissions(this)
            .request(Manifest.permission.READ_EXTERNAL_STORAGE)
            .subscribe {
                if (it) {
                    try {
                        contentResolver.openInputStream(uri).use { input ->
                            importCustomizeConfig(input?.bufferedReader()?.readText())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else
                    toast(R.string.toast_permission_denied)
            }
    }

    /**
     * import customize config
     */
    fun importCustomizeConfig(server: String?) {
        try {
            if (server == null || TextUtils.isEmpty(server)) {
                toast(R.string.toast_none_data)
                return
            }
            mainViewModel.appendCustomConfigServer(server)
            mainViewModel.reloadServerList()
            toast(R.string.toast_success)
            //adapter.notifyItemInserted(mainViewModel.serverList.lastIndex)
        } catch (e: Exception) {
            ToastCompat.makeText(
                this,
                "${getString(R.string.toast_malformed_josn)} ${e.cause?.message}",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
            return
        }
    }

    fun setTestState(content: String?) {
        binding.tvTestState.text = content
    }

//    val mConnection = object : ServiceConnection {
//        override fun onServiceDisconnected(name: ComponentName?) {
//        }
//
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            sendMsg(AppConfig.MSG_REGISTER_CLIENT, "")
//        }
//    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    fun showCircle() {
//        binding.fabProgressCircle.show()
        binding.progress.isVisible = true
    }

    fun hideCircle() {
        if (binding.progress.isShown) {
            binding.progress.isVisible = false
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}