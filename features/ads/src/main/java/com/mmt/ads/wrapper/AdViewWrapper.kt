package com.mmt.ads.wrapper

import android.content.Context
import android.view.ViewGroup
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.mmt.ads.AdsConstants
import com.mmt.ads.admob.AdmobLoader
import com.mmt.ads.config.AdsConfig
import com.mmt.ads.utils.AdDebugLog
import com.mmt.ads.utils.AdsUtils

class AdViewWrapper(context: Context, adId: String) : AdWrapper(context, adId) {

    init {
        TAG = "[${this::class.java.simpleName}] ${hashCode()} -- "
    }

    private var mAdView: AdView? = null

    fun showBottomBanner(container: ViewGroup? = null) {
        updateContainer(container)
        if (!checkConditions()) return

        initNormalAdView()
    }

    fun showMediumBanner(container: ViewGroup? = null) {
        updateContainer(container)
        if (!checkConditions()) return

        initMediumAdView()
    }

    private fun getAdId(): String {
        return if (AdsConfig.getInstance().isTestMode) {
            AdsConstants.banner_test_id
        } else {
            mAdId
        }
    }

    /**
     * Init & show adaptive banner for empty screen | exit dialog
     * size: fit screen width, height ~60dp
     * */
    private fun initNormalAdView() {
        isLoading = true
        isLoaded = false

        val adId = getAdId()
        val adListener = object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                val message = if (error.message.isNotEmpty()) "\nErrorMsg: ${error.message}" else ""
                val errorMsg = "\nErrorCode: ${error.code}" + message + "\nid: $adId"
                AdDebugLog.loge("$TAG NormalAdView $errorMsg")
                // Mark flag for this id just load failed
                AdsConfig.getInstance().onAdFailedToLoad(mAdId)
                // Notify load failed
                notifyAdLoadFailed(error.code)
                // removeAdFromContainer
                removeAdsFromContainer()
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                AdDebugLog.logd("$TAG NormalAdView loaded - mAdId: $mAdId")
                // Mark flag for this id just loaded
                AdsConfig.getInstance().onAdLoaded(mAdId)
                // Notify Ad loaded
                notifyAdLoaded()
                // Add Ad to container
                addAdsToContainer()
            }

            override fun onAdClicked() {
                super.onAdClicked()
                // Notify event
                notifyAdClicked()
                // Reload Ad
                reloadWhenAdClicked(true)
            }
        }

        mContext?.let { mAdView = AdmobLoader.initAdaptiveBanner(it.applicationContext, adId, adListener) }
    }

    /**
     * Init & show medium banner for empty screen | exit dialog
     * size: 300 x 250
     * */
    private fun initMediumAdView() {
        isLoading = true
        isLoaded = false

        val adId = getAdId()
        val adListener = object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                val message = if (error.message.isNotEmpty()) "\nErrorMsg: ${error.message}" else ""
                val errorMsg = "\nErrorCode: ${error.code}" + message + "\nid: $adId"
                AdDebugLog.loge("$TAG MediumAdView $errorMsg")
                // Mark flag for this id just load failed
                AdsConfig.getInstance().onAdFailedToLoad(mAdId)
                // Notify load failed
                notifyAdLoadFailed(error.code)
                // removeAdFromContainer
                removeAdsFromContainer()
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                AdDebugLog.logd("$TAG MediumAdView loaded - mAdId: $mAdId")
                // Mark flag for this id just loaded
                AdsConfig.getInstance().onAdLoaded(mAdId)
                // Notify Ad loaded
                notifyAdLoaded()
                // Add Ad to container
                addAdsToContainer()
            }

            override fun onAdClicked() {
                super.onAdClicked()
                // Notify event
                notifyAdClicked()
                // Reload Ad
                reloadWhenAdClicked(false)
            }
        }

        mContext?.let { mAdView = AdmobLoader.initMediumBanner(it.applicationContext, adId, adListener) }
    }

    /**
     * Reload when Ad clicked
     * */
    private fun reloadWhenAdClicked(isBottomBanner: Boolean) {
        removeAdsFromContainer()
        destroyAdInstance()
        if (isBottomBanner) {
            showBottomBanner()
        } else {
            showMediumBanner()
        }
    }

    override fun addAdsToContainer() {
        AdsUtils.addAdsToContainer(getContainer(), mAdView)
    }

    override fun removeAdsFromContainer() {
        mAdView?.apply {
            if (parent is ViewGroup) {
                (parent as ViewGroup).removeAllViews()
            }
        }
    }

    override fun destroyAdInstance() {
        // Set flags
        isLoading = false
        isLoaded = false
        // Destroy Ad instance
        mAdView?.destroy()
        mAdView = null
    }
}