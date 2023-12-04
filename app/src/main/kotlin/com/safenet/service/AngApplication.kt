package com.safenet.service

import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import okhttp3.internal.http2.Http2
import okhttp3.internal.platform.Platform
import okhttp3.internal.platform.android.AndroidLogHandler
import timber.log.Timber
import timber.log.Timber.DebugTree
import timber.log.Timber.Forest.plant
import java.util.logging.Logger


@HiltAndroidApp
class AngApplication : MultiDexApplication() {
    companion object {
        const val PREF_LAST_VERSION = "pref_last_version"
    }

    private var firstRun = false
        private set

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            plant(DebugTree())
        } else {
            plant(CrashReportingTree())
            Platform.get() // Initialize Platform
            listOfNotNull(
                OkHttpClient::class.java.`package`?.name,
                OkHttpClient::class.java.name,
                Http2::class.java.name,
            ).forEach {
                Logger.getLogger(it).removeHandler(AndroidLogHandler)
            }
        }


//        LeakCanary.install(this)

        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        firstRun = defaultSharedPreferences.getInt(PREF_LAST_VERSION, 0) != BuildConfig.VERSION_CODE
        if (firstRun)
            defaultSharedPreferences.edit().putInt(PREF_LAST_VERSION, BuildConfig.VERSION_CODE).apply()

        //Logger.init().logLevel(if (BuildConfig.DEBUG) LogLevel.FULL else LogLevel.NONE)
        MMKV.initialize(this)
    }

    class CrashReportingTree : Timber.Tree() {

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {

        }

    }
}
