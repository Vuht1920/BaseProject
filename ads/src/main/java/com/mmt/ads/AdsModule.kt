package com.mmt.ads

import android.app.Application
import android.os.SystemClock
import com.google.android.gms.ads.MobileAds
import com.mmt.ads.config.AdsConfig
import com.mmt.ads.models.AdsId
import com.mmt.ads.models.LoadingState
import com.mmt.ads.utils.AdDebugLog
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AdsModule() {
    companion object {
        private val sInstance: AdsModule by lazy { AdsModule() }

        fun getInstance(): AdsModule {
            return sInstance
        }
    }

    private var mAdsId: AdsId? = null
    private var mLoadingState = LoadingState.NONE
    private var mSession = 0
    private var mApplication: Application? = null

    fun mustInit(): Boolean {
        return mApplication == null || mAdsId == null
    }

    fun initialize(application: Application) {
        mApplication = application

    }

    /**
     * Set Application Context & initialize modules
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun init(application: Application, callback: InitCallback? = null): AdsModule {
        try {
            mApplication = application
            if (mLoadingState == LoadingState.NONE) {
                mLoadingState = LoadingState.LOADING
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val start = SystemClock.elapsedRealtime()
                        MobileAds.initialize(application) {
                            mLoadingState = LoadingState.FINISHED
                            AdDebugLog.loge("MobileAds initializationCompleted -> Take " + (SystemClock.elapsedRealtime() - start) + " ms")
                            GlobalScope.launch(Dispatchers.Main) {
                                callback?.onInitializeCompleted()
                            }
                        }
                        MobileAds.setAppMuted(true)
                        MobileAds.setAppVolume(0.0f)
                    } catch (e: Exception) {
                        AdDebugLog.loge(e)
                    }
                }
            }

            AdsConfig.getInstance().initAdsState(application)

//            if (mAdsIdConfigList.isEmpty()) {
//                initResources(application)
//            }

            if (initializeCompleted()) {
                callback?.onInitializeCompleted()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this@AdsModule
    }

    private fun initializeCompleted(): Boolean {
        return mLoadingState == LoadingState.FINISHED
    }

    interface InitCallback {
        fun onInitializeCompleted()
    }
}