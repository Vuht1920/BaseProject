package com.mmt.ads.wrapper

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import com.mmt.ads.config.AdsConfig
import com.mmt.ads.utils.AdDebugLog
import java.lang.ref.WeakReference

abstract class AdWrapper(context: Context, val mAdId: String): AbsAdListeners() {
    open var TAG = "[${this::class.java.simpleName}] -- "

    abstract fun destroyAdInstance()
    abstract fun addAdsToContainer()

    private var mAdsContainer: WeakReference<ViewGroup?>? = null
    open var mContext: Context? = context
    open var isLoading: Boolean = false
    open var isLoaded: Boolean = false
    open val mHandler = Handler(Looper.getMainLooper())

    fun updateContainer(container: ViewGroup?) {
        container?.let { mAdsContainer = WeakReference(it) }
    }

    fun deleteContainer() {
        getContainer()?.removeAllViews()
        mAdsContainer?.clear()
        mAdsContainer = null
    }

    fun getContainer(): ViewGroup? {
        return mAdsContainer?.get()
    }

    fun checkConditions(): Boolean {
        if (!AdsConfig.getInstance().canShowAd()) return false

        if (mContext == null) return false

        if (isLoading) {
//            AdDebugLog.logd("$TAG RETURN when Ad loading")
            return false
        }

        if (AdsConfig.getInstance().cantLoadId(mAdId)) {
            AdDebugLog.loge("$TAG RETURN because this id just failed to load\nid: $mAdId")
            notifyAdLoadFailed()
            return false
        }

        if (isLoaded) {
//            AdDebugLog.logd("$TAG Ad loaded -> show Ad immediate")
            // Show Ads immediate
            addAdsToContainer()
            // Notify AdLoaded
            notifyAdLoaded()
            return false
        }
        return true
    }

    override fun notifyAdLoaded() {
        // Mark flag isLoading & isLoaded
        isLoaded = true
        isLoading = false

        super.notifyAdLoaded()
    }

    override fun notifyAdLoadFailed(errorCode: Int) {
        // Mark flag isLoading & isLoaded
        isLoaded = false
        isLoading = false

        super.notifyAdLoadFailed(errorCode)
    }

    open fun removeAdsFromContainer(){}

    /**
    * Destroy Ad
    * */
   open fun destroy() {
        AdDebugLog.logi("$TAG Destroy Ad")
        // Set flags
        isLoading = false
        isLoaded = false
        // Remove callback
        mHandler.removeCallbacksAndMessages(null)
        // Remove Ads from container
        removeAdsFromContainer()
        // Delete Ad
        destroyAdInstance()
        // Delete container
        deleteContainer()
        // Clear listeners
        mAdListeners.clear()
    }
}