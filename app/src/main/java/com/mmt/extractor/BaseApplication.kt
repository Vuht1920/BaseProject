package com.mmt.extractor

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.mmt.ads.config.AdsConfig
import com.mmt.extractor.data.repository.dataStore.PrefDataStore
import com.mmt.extractor.utils.exception.UnCaughtException
import com.mmt.extractor.utils.log.DebugLog
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject


@HiltAndroidApp
class BaseApplication : AbsApplication() {
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (BuildConfig.DEBUG) {
            throwable.printStackTrace()
        }
        DebugLog.loge(throwable)
//        FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    val IOScope = Dispatchers.IO + coroutineExceptionHandler
    val MainScope = Dispatchers.Main + coroutineExceptionHandler

    companion object {
        lateinit var instance: BaseApplication
        fun coroutineExceptionHandler() = instance.coroutineExceptionHandler
    }

    @Inject
    lateinit var prefDataStore: PrefDataStore

    @Inject
    lateinit var unCaughtException: UnCaughtException

    override fun onCreate() {
        super.onCreate()
        instance = this
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        DebugLog.DEBUG = BuildConfig.DEBUG || BuildConfig.TEST_AD // Đặt flag cho DebugLog (Chỉ hiển thị log trong bản build Debug | TEST_AD)

        // Init AdsConfigs
        initAdsConfig(this)

        initCrash()

    }

    /**
     * Tự động restart app khi bị crash
     * */
    private fun initCrash() {
        if (!BuildConfig.DEBUG && !BuildConfig.TEST_AD) {
            Thread.setDefaultUncaughtExceptionHandler(unCaughtException)
        }
    }

    fun initAdsConfig(application: Application?) {
        if (application != null) {
            val isTestMode = BuildConfig.DEBUG || BuildConfig.TEST_AD
            AdsConfig.getInstance()
                .init(application)
                /*
                                ._setFullVersion(PreferenceHelper.isPremiumPurchased)
                */
                ._setTestGDPR(isTestMode)
                ._setTestMode(isTestMode)
                .setShowLog(isTestMode)
                .setAdsEnableState(TestConfig.DEFAULT_ADS_ENABLE_STATE)
            /*.setWaitingTimeWhenLoadFailed(FirebaseRemoteConfigHelper.instance.getWaitingTimeWhenLoadAdsFailedInMs())
            .setAdsEnableState(FirebaseRemoteConfigHelper.instance.getADsEnableState())
            .setFreqInterOPAInMs(FirebaseRemoteConfigHelper.instance.freqInterOPAInMs)
            .setSplashDelayInMs(FirebaseRemoteConfigHelper.instance.interOPASplashDelayInMs)
            .setInterOPAProgressDelayInMs(FirebaseRemoteConfigHelper.instance.interOPAProgressDelayInMs)*/
        }
    }
}