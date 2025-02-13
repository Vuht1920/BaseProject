package com.mmt.app

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mmt.BuildConfig
import com.mmt.ads.AdsModule
import com.mmt.ads.config.AdsConfig
import com.mmt.app.utils.exception.UnCaughtException
import com.mmt.app.utils.log.DebugLog
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock

@HiltAndroidApp
class BaseApplication : AbsApplication() {
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        DebugLog.loge(throwable)
//        FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    companion object {
        var instance: BaseApplication? = null
        private const val ACTION_REFRESH_APPLICATION_INSTANCE = "ACTION_REFRESH_APPLICATION_INSTANCE"

        fun coroutineExceptionHandler() = instance?.coroutineExceptionHandler ?: CoroutineExceptionHandler { _, throwable -> DebugLog.loge(throwable) }

        val IODispatcher by lazy { Dispatchers.IO + coroutineExceptionHandler() }
        val MainDispatcher by lazy { Dispatchers.Main + coroutineExceptionHandler() }

        fun refreshInstanceIfNeed(context: Context) {
            try {
                if (instance == null) {
                    val intent = Intent(ACTION_REFRESH_APPLICATION_INSTANCE)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                }
            } catch (e: Exception) {
                DebugLog.loge(e)
            }
        }

        fun initAdsConfig(application: Application?) {
            application?.let {
                val isTestMode = BuildConfig.DEBUG || BuildConfig.TEST_AD
                AdsConfig.getInstance().init(application)
                    ._setFullVersion(false)
                    ._setTestMode(isTestMode)
                    ._setShowLog(isTestMode)
                    ._setTestGDPR(isTestMode)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        val isTestMode = BuildConfig.DEBUG || BuildConfig.TEST_AD
        DebugLog.DEBUG = isTestMode// Đặt flag cho DebugLog (Chỉ hiển thị log trong bản build Debug | TEST_AD)

        // Cần phải init prevent exception trước khi init các thành phần khác
        if (!isTestMode) {
            Thread.setDefaultUncaughtExceptionHandler(UnCaughtException(this))
        }
        // Init AdsConfigs
        initAdsConfig(this)

        // Init AdsModule
        if (BuildConfig.SHOW_AD) {
            AdsModule.getInstance().initResources(this)
                .setAdsIdListConfig(FirebaseRemoteConfigHelper.instance.adsIdList)
                .setCustomAdsIdListConfig(FirebaseRemoteConfigHelper.instance.customAdsIdList)
        }

        registerReceiverRefreshStaticInstance()
    }

    /**
     * Receiver to refresh Application instance
     * */
    private fun registerReceiverRefreshStaticInstance() {
        try {
            val intentFilter = IntentFilter(ACTION_REFRESH_APPLICATION_INSTANCE)
            LocalBroadcastManager.getInstance(this).registerReceiver(receiverRefreshInstance, intentFilter)
        } catch (e: java.lang.Exception) {
            DebugLog.loge(e)
        }
    }

    private fun unregisterReceiverRefresh() {
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverRefreshInstance)
        } catch (e: java.lang.Exception) {
            DebugLog.loge(e)
        }
    }

    private val receiverRefreshInstance: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            instance = this@BaseApplication
            if (AdsConfig.getInstance().mustInit()) {
                initAdsConfig(this@BaseApplication)
            }
            if (AdsModule.getInstance().mustInit()) {
                AdsModule.getInstance().init(this@BaseApplication)
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterReceiverRefresh()
        destroy()
    }
}