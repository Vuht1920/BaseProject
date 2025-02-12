package com.mmt.ads.wapper

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.ViewGroup
import com.mmt.ads.config.AdsConfig
import com.mmt.ads.utils.AdDebugLog
import com.mmt.ads.utils.AdsUtils

abstract class AdWrapper(context: Context, val mAdId: String) : AbsAdListeners() {
    open var TAG = "[${this::class.java.simpleName}] -- "

    abstract fun destroyAdInstance()
    abstract fun addAdsToContainer()

    protected var mAdsContainer: ViewGroup? = null
    open var mContext: Context? = context
    open var isLoading: Boolean = false
    open var isLoaded: Boolean = false
    open val mHandler = Handler(Looper.getMainLooper())

    open var loadedTimestamp: Long = 0L

    open fun getCacheTime(): Long {
        return 0
    }

    open fun visibleAds() {}
    open fun invisibleAds() {}

    fun updateContainer(container: ViewGroup?) {
        container?.let { mAdsContainer = it }
    }

    fun deleteContainer() {
        wrapHeightForContainer()
        getContainer()?.removeAllViews()
        mAdsContainer = null
    }

    fun getContainer(): ViewGroup? {
        return mAdsContainer
    }

    fun checkConditions(): Boolean {
        if (!AdsConfig.getInstance().canShowAd()) {
            wrapHeightForContainer()
            return false
        }

        if (mContext == null) {
            wrapHeightForContainer()
            return false
        }

        if (isLoading) {
//            AdDebugLog.logd("$TAG RETURN when Ad loading \nid: $mAdId")
            showLoadingView(getContainer())
            return false
        }

        if (AdsConfig.getInstance().cantLoadId(mAdId)) {
            AdDebugLog.loge("$TAG RETURN because this id just failed to load \nid: $mAdId")
            wrapHeightForContainer()
            notifyAdLoadFailed(mAdId, 404, "RETURN because this id just failed to load")
            return false
        }

        if (isAdAvailable()) {
//            AdDebugLog.logd("$TAG Ad loaded -> show Ad immediate")
            // Show Ads immediate
            addAdsToContainer()
            // Notify AdLoaded
            notifyAdLoaded(mAdId)
            return false
        }
        return true
    }

    fun wrapHeightForContainer() {
        // Set height cho container là WrapContent
        AdsUtils.setHeightForContainer(mAdsContainer, 0)
    }

    open fun isAdAvailable(): Boolean {
        return if (getCacheTime() > 0) {
            val dateDifference = SystemClock.elapsedRealtime() - loadedTimestamp
            val hasCache = dateDifference < getCacheTime()
//            if (hasCache) AdDebugLog.logd("hasCachedAd - passed time from loaded: ${dateDifference/1000} s")
            isLoaded && hasCache
        } else {
            isLoaded
        }
    }

    open fun adLoaded() {
        loadedTimestamp = SystemClock.elapsedRealtime()
        notifyAdLoaded(mAdId)
    }

    open fun adLoadFailed(errorCode: Int) {
        loadedTimestamp = 0
        notifyAdLoadFailed(mAdId, errorCode, "")
        wrapHeightForContainer()
    }

    override fun notifyAdLoaded(id: String) {
        // Mark flag isLoading & isLoaded
        isLoaded = true
        isLoading = false

        super.notifyAdLoaded(id)
    }

    override fun notifyAdLoadFailed(id: String, errorCode: Int, errorMsg: String) {
        // Mark flag isLoading & isLoaded
        isLoaded = false
        isLoading = false

        super.notifyAdLoadFailed(id, errorCode, errorMsg)
    }

    open fun removeAdsFromContainer() {}

    open fun showLoadingView(container: ViewGroup?) {}

    /**
     * Destroy Ad
     * */
    open fun destroy() {
        AdDebugLog.logi("$TAG Destroy Ad")
        // Set flags
        isLoading = false
        isLoaded = false
        loadedTimestamp = 0
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