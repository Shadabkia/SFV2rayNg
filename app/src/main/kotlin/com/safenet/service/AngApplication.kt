package com.safenet.service

import androidx.hilt.work.HiltWorker
import androidx.hilt.work.HiltWorkerFactory
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import androidx.work.Configuration
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.security.Provider
import javax.inject.Inject

@HiltAndroidApp
class AngApplication : MultiDexApplication(), Configuration.Provider {

    @Inject
    lateinit var workerFactory : HiltWorkerFactory
    companion object {
        const val PREF_LAST_VERSION = "pref_last_version"
    }

    var firstRun = false
        private set

    override fun getWorkManagerConfiguration(): Configuration {
        return if (BuildConfig.DEBUG) {
            Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .setWorkerFactory(workerFactory)
                .build()
        } else {
            Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.ERROR)
                .setWorkerFactory(workerFactory)
                .build()
        }
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

//        LeakCanary.install(this)

        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        firstRun = defaultSharedPreferences.getInt(PREF_LAST_VERSION, 0) != BuildConfig.VERSION_CODE
        if (firstRun)
            defaultSharedPreferences.edit().putInt(PREF_LAST_VERSION, BuildConfig.VERSION_CODE).apply()

        //Logger.init().logLevel(if (BuildConfig.DEBUG) LogLevel.FULL else LogLevel.NONE)
        MMKV.initialize(this)
    }
}
