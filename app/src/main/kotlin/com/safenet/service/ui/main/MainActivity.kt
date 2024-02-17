package com.safenet.service.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.KeyEvent
import android.view.Menu
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.safenet.service.AppConfig
import com.safenet.service.AppConfig.ANG_PACKAGE
import com.safenet.service.BuildConfig
import com.safenet.service.R
import com.safenet.service.databinding.ActivityMainBinding
import com.safenet.service.extension.toast
import com.safenet.service.extension.toastLong
import com.safenet.service.helper.SimpleItemTouchHelperCallback
import com.safenet.service.ui.BaseActivity
import com.safenet.service.ui.MainRecyclerAdapter
import com.safenet.service.ui.ServerActivity
import com.safenet.service.ui.on_boarding.OnBoardingActivity
import com.safenet.service.ui.on_boarding.voucher_bottomsheet.EnterVoucherBottomSheetViewModel
import com.safenet.service.util.*
import com.tbruyelle.rxpermissions.RxPermissions
import com.tencent.mmkv.MMKV
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
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

    val mainViewModel: MainViewModel by viewModels()

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
    private val requestVpnPermission by lazy {
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            binding.fab.isEnabled = true
        }
    }

    private var mItemTouchHelper: ItemTouchHelper? = null

    val defaultSharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(
            this@MainActivity
        )
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel.checkIsAppActive()

        lifecycleScope.launch {
            mainViewModel.isAppActive.collectLatest {
                if (!it) {
                    startActivity(Intent(this@MainActivity, OnBoardingActivity::class.java))
                    finish()
                }
            }
        }

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
                    MainActivityEvents.InitViews -> initView()
                    is MainActivityEvents.ActivateApp -> activateApp(event.status)
                    is MainActivityEvents.GetConfigMessage -> {
                        hideCircle(1)
                        binding.fab.isEnabled = true
                        toastLong(event.message ?: "Error")
                    }

                    is MainActivityEvents.Disconnected -> {
                        binding.fab.isEnabled = true
                    }

                    MainActivityEvents.ShowLogoutDialog -> showLogoutDialog()
                    is MainActivityEvents.ShowMessage -> toast(event.message)
                    MainActivityEvents.ShowTimeDialog -> showTimeErrorDialog()
                    MainActivityEvents.MaxLoginDialog -> showMaxLoginDialog()
                    is MainActivityEvents.ShowMessageDialog -> showNotificationDialog(event.message)

                    MainActivityEvents.HideCircle -> {
                        hideCircle(2)
                    }

                    is MainActivityEvents.ShowUpdateUI -> showUpdateUI(event.status)
                    is MainActivityEvents.OpenBrowser -> Utils.openWebPage(
                        this@MainActivity,
                        event.link
                    )

                    MainActivityEvents.DownloadFailed -> downloadStatus(DownloadAppStatus.FAILED, 0)
                    is MainActivityEvents.DownloadFinished -> downloadStatus(
                        DownloadAppStatus.FINISHED,
                        event.progress
                    )

                    MainActivityEvents.DownloadStarted -> downloadStatus(
                        DownloadAppStatus.STARTED,
                        0
                    )

                }

            }
        }
        mainViewModel.activityCreated()
    }

    private fun showUpdateUI(status: Boolean) {
        when (mainViewModel.savedStateHandle.get<DownloadAppStatus>("downloading")) {
            DownloadAppStatus.STARTED -> {
                Timber.tag("showUpdateUI").d("showUpdateUI STARTED")
                binding.btUpdate.isVisible = false
                binding.clDownloadProgressbar.isVisible = true
            }

            DownloadAppStatus.FINISHED, DownloadAppStatus.FAILED -> {
                Timber.tag("showUpdateUI").d("showUpdateUI FAILED")
                binding.apply {
                    btUpdate.isVisible = true
                    clDownloadProgressbar.isVisible = false
                }
            }

            else -> {
                Timber.tag("showUpdateUI").d("showUpdateUI $status")
                binding.btUpdate.isVisible = status
            }
        }
    }

    private fun downloadStatus(downloadStatus: DownloadAppStatus, progress: Int) {
        Timber.d("downloadStatus $downloadStatus")
        binding.btUpdate.isEnabled = true
        when (downloadStatus) {
            DownloadAppStatus.STARTED -> {
                toastLong("app is disabled during download")
                binding.apply {
                    fab.isEnabled = false
                    btUpdate.isVisible = false
                    clDownloadProgressbar.isVisible = true
                }
            }

            DownloadAppStatus.FINISHED -> {
                if (progress == 100) {
                    toast("app is enabled")
                    binding.apply {
                        fab.isEnabled = true
                        btUpdate.isVisible = true
                        clDownloadProgressbar.isVisible = false
                        // install app and restart
                        requestPackageInstallationsPermission()
                        // change update status
                        // delete update link
                    }
                } else {
                    toastLong("download failed")
                    binding.apply {
                        fab.isEnabled = true
                        btUpdate.isVisible = true
                        clDownloadProgressbar.isVisible = false
                        // delete imperfect app
                        deleteAppFile(mainViewModel.appFileName)
                    }
                }
            }

            DownloadAppStatus.FAILED -> {
                toastLong("download failed")
                binding.apply {
                    fab.isEnabled = true
                    btUpdate.isVisible = true
                    clDownloadProgressbar.isVisible = false
                    // delete imperfect app
                    deleteAppFile(mainViewModel.appFileName)
                }
            }

        }

    }

    private fun showMaxLoginDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage(R.string.max_logout_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->

            }
            .show()
    }

    private fun showNotificationDialog(message: String?) {

        binding.apply {
            if (message.isNullOrEmpty() || message.lowercase() == "ok")
                cvNotification.visibility = View.GONE
            else {
                tvNotification.text = message
                if (cvNotification.visibility == View.GONE) {
                    //Load animation
                    val slideDown: Animation = AnimationUtils.loadAnimation(
                        applicationContext,
                        R.anim.slide_down
                    )

                    cvNotification.startAnimation(slideDown)
                    cvNotification.visibility = View.VISIBLE
                }
            }
        }


    }

    private fun showTimeErrorDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage(R.string.time_error_dialog)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                // go to time setting
                startActivity(Intent(Settings.ACTION_DATE_SETTINGS));
            }
            .show()
    }

    private fun showLogoutDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage(R.string.logout_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                mainViewModel.disconnectAndLogout(this@MainActivity)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->

            }
            .show()
    }

    private fun activateApp(status: Boolean) {
        Timber.tag("ACTIVATE").d("activateApp $status")
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

    @SuppressLint("SetTextI18n")
    private fun initView() {
        listeners()

        binding.navToolbar.safenet.text =
            getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME

        this.lifecycleScope.launch {
            mainViewModel.config.collectLatest {
                Timber.tag("ConfigApi").d("config : $it")
                if (it.isNotEmpty()) {
                    importClipboard(it)
                }
                binding.fab.isEnabled = true
            }
        }

        this@MainActivity.lifecycleScope.launch {
            mainViewModel.serverAvailability.collect {
                binding.serverAvailability.text = it
            }

        }

        this.lifecycleScope.launch {
            mainViewModel.downloadPercentage.collectLatest { percentage ->
                if (percentage != null) {
                    binding.tvPercentage.text = "$percentage%"
                }
            }
        }

        this.lifecycleScope.launch {
            mainViewModel.isUpdateRequired.collectLatest { isRequired ->
                if (isRequired) {
                    binding.fab.isEnabled = false
                    binding.fab.backgroundTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.colorUpdate
                            )
                        )
                    toastLong("Please Update the app")
                }
            }
        }

        this.lifecycleScope.launch {
            ApiUrl.base_url_counter.collectLatest {
                if (it > 8) {
                    mainViewModel.getBaseAddress()
                }
            }
        }
        requestNotificationPermission()
        deleteAppFile("SafeNet-${BuildConfig.VERSION_CODE}.apk")
    }

    private fun listeners() {
        binding.apply {
            navToolbar.apply {
                activeVpn.setOnClickListener {
                    if ((it as MaterialButton).text == getString(R.string.logout)) {
                        mainViewModel.onLogoutClicked()
                    } else{
                        //                        mainViewModel.onActiveVpnClicked(this@MainActivity)
                    }
                }
            }

            contactUs.setOnClickListener {
                val intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/safenet_vpn_admin"))
                startActivity(intent)
            }

            fab.setOnClickListener { view ->
                this@MainActivity.lifecycleScope.launch {
                    if (mainViewModel.isUpdateRequired.value) {
                        toastLong("Please Update the app")
                        return@launch
                    }
                    if (mainViewModel.isRunning.value == true) {
                        Utils.stopVService(this@MainActivity)
                        hideCircle(3)
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

            btUpdate.setOnClickListener {
                this@MainActivity.lifecycleScope.launch {
                    if (File(
                            application.getExternalFilesDir(null),
                            mainViewModel.appFileName
                        ).exists()
                    )
                        installUpdatedAPK()
                    else {
                        mainViewModel.downloadAPKFromServer(this@MainActivity)
                        btUpdate.isEnabled = false
                    }
                }
            }

            copyVoucher.setOnClickListener {
                Timber.tag("testtt").d("copyVoucher")
                val job = lifecycleScope.launch {
                    if (isActive)
                        mainViewModel.copyToClipboard(this@MainActivity)
                }
                job.cancel()
            }

            serverAvailability.setOnClickListener {
                mainViewModel.onServersClicked(this@MainActivity)
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
                Timber.tag("ACTIVATE").d("onBindViewHolder")
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
                            .i("Copied from apk assets folder to %s", target.absolutePath)
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
        mainViewModel.updateServerName()
    }

    public override fun onPause() {
        super.onPause()
        hideCircle(4)
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
    private suspend fun importClipboard(config: String)
            : Boolean {
        try {
//            val clipboard = Utils.getClipboard(this)
            val decodeConfig = KeyManage.instance.getConfig(config)
            Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("config : $decodeConfig")
            mainViewModel.importBatchConfig(decodeConfig, "", this@MainActivity)
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.tag(EnterVoucherBottomSheetViewModel.TAG).d("error : ${e.message}")
            toastLong(R.string.wrong_config)
            hideCircle(0)
            mainViewModel.setAppActivated(false)
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

    fun hideCircle(int: Int) {
        Timber.tag("circle").d("hideCircle! $int")
        if (binding.progress.isShown) {
            binding.progress.isVisible = false
        }
    }

    private fun requestPackageInstallationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                intent.data = Uri.parse("package:$packageName")
                requestInstallPackagesLauncher.launch(intent)
            } else {
                installUpdatedAPK()
            }
        }
    }

    private val requestInstallPackagesLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            toast("installing ...")
            installUpdatedAPK()
        } else {
            toast("Permission is necessary for updating app")
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (PackageManager.PERMISSION_GRANTED !=
                packageManager.checkPermission(Manifest.permission.POST_NOTIFICATIONS, packageName)
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
            } else {
                toastLong("Please turn on notification in App setting")
            }
        }

    private fun installUpdatedAPK() {
        try {
            val apkFile = File(application.getExternalFilesDir(null), mainViewModel.appFileName)

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(this, "$packageName.provider", apkFile)
            } else {
                Uri.fromFile(apkFile)
            }

            val installIntent = Intent(Intent.ACTION_VIEW)
            installIntent.setDataAndType(uri, "application/vnd.android.package-archive")
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            requestInstallAppLauncher.launch(installIntent)

        } catch (e: Exception) {
            toast("unable to install: ${e.message}")
        }
    }

    private val requestInstallAppLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            toast("app updated")
            deleteAppFile(mainViewModel.appFileName)
            mainViewModel.showUpdateUI(false)
        } else {
//            toast("not installed")
        }
    }

    private fun deleteAppFile(appName: String) {
        try {
            Timber.tag("downloads").d("deleteAppFile $appName")
            val apkFile = File(application.getExternalFilesDir(null), appName)
            if (apkFile.exists())
                apkFile.delete()
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteAppFile(mainViewModel.appFileName)
    }

}
