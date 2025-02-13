package com.mmt.ads.wapper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ScreenUtils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.mmt.AdContainerWatcher
import com.mmt.ads.AdmobLoader
import com.mmt.ads.R
import com.mmt.ads.config.AdsConfig
import com.mmt.ads.utils.AdDebugLog
import com.mmt.ads.utils.AdsUtils
import com.tohsoft.ads.AdsModule
import java.util.concurrent.atomic.AtomicBoolean

/**
* AdView bottom theo logic riêng cho một số MH. Start loadAd khi vào MH, pause/resume theo trạng thái MH, destroy khi MH destroyed.
* */
class AdViewBottom(val context: Context, private val adId: String, lifecycle: Lifecycle, private val adContainer: ViewGroup) : AdWrapper(context, adId) {
    private val mDefaultBottomAdHeight = ConvertUtils.dp2px(60f)
    private var mAdView: AdView? = null
    private var mLoadingView: View? = null
    private var mBottomAdHeight = 0

    private val initialLayoutComplete = AtomicBoolean(false)

    private val defaultLifecycle = object : DefaultLifecycleObserver {
        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            AdDebugLog.logi("$TAG onPause")
            mAdView?.pause()
        }

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            AdDebugLog.logi("$TAG onResume")
            mAdView?.resume()
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            AdDebugLog.logi("$TAG onDestroy")
            mAdView?.destroy()
            AdsModule.getInstance().destroyAdViewBottom(adContainer)
            owner.lifecycle.removeObserver(this)
        }
    }

    init {
        TAG = "[${this::class.java.simpleName}] ${hashCode()} -- "

        lifecycle.addObserver(defaultLifecycle)

        updateContainer(adContainer)
        AdContainerWatcher.add(adContainer)
        // Since we're loading the banner based on the adContainerView size, we need to wait until this
        // view is laid out before we can get the width.
        adContainer.viewTreeObserver.addOnGlobalLayoutListener {
            if (!initialLayoutComplete.getAndSet(true) && AdsConfig.getInstance().canShowAd()) {
                loadBanner()
            }
        }
    }

    override fun getCacheTime(): Long {
        return if (AdsConfig.getInstance().isTestCacheAdsTime) 120_000 else 3600000 // 1 hour
    }

    override fun visibleAds() {
        mAdView?.visibility = View.VISIBLE
    }

    override fun invisibleAds() {
        mAdView?.visibility = View.INVISIBLE
    }

    // Load an ad.
    fun showAd() {
        if (initialLayoutComplete.get()) {
            loadBanner()
        }
    }

    private fun loadBanner() {
        if (!checkConditions()) return
        if (!NetworkUtils.isConnected()) {
            wrapHeightForContainer()
            return
        }
        if (AdsConfig.getInstance().canShowAd()) {
            initNormalAdView()
        } else {
            adContainer.removeAllViews()
        }
    }

    /**
     * Init & show adaptive banner for bottom screen
     * size: fit screen width, height ~60dp
     * */
    private fun initNormalAdView() {
        isLoading = true
        isLoaded = false
        loadedTimestamp = 0

        val adListener = object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                val message = if (error.message.isNotEmpty()) "\nErrorMsg: ${error.message}" else ""
                val errorMsg = "\nErrorCode: ${error.code}" + message + "\nid: $adId"
                AdDebugLog.loge("$TAG NormalAdView $errorMsg")
                mBottomAdHeight = 0
                // Mark flag for this id just load failed
                AdsConfig.getInstance().onAdFailedToLoad(mAdId)
                // Notify load failed
                adLoadFailed(error.code)
                // removeAdFromContainer
                removeAdsFromContainer()
                // Hide loading view
                hideLoadingView(removeImmediate = true)
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                AdDebugLog.logd("$TAG NormalAdView loaded - mAdId: $mAdId")
                // Mark flag for this id just loaded
                AdsConfig.getInstance().onAdLoaded(mAdId)
                // Notify Ad loaded
                adLoaded()
                // Add Ad to container
                addAdsToContainer()
            }

            override fun onAdClicked() {
                super.onAdClicked()
                // Notify event
                notifyAdClicked()
                // Reload Ad
                reloadWhenAdClicked()
            }
        }

        AdDebugLog.logi(TAG + "Start load NormalAdView id $adId")
        mContext?.let { mAdView = AdmobLoader.initAdaptiveBanner(it.applicationContext, adId, adListener) }

        // Show loading
        showLoadingView(container = getContainer())
    }

    private fun validateAdHeight() {
        if (mAdView?.measuredHeight != 0) {
            mBottomAdHeight = mAdView?.measuredHeight ?: 0
            AdDebugLog.logi("$TAG \nmAdView?.measuredHeight = ${mAdView?.measuredHeight}")
            if (mBottomAdHeight == 0) mBottomAdHeight = mDefaultBottomAdHeight
        }
    }

    /**
     * Loading view (place holder)
     * */
    override fun showLoadingView(container: ViewGroup?) {
        if (mLoadingView == null) {
            mLoadingView = LayoutInflater.from(mContext).inflate(R.layout.banner_ad_bottom_loading, null)
        }

        val loadingParentView = mLoadingView!!.parent
        if (loadingParentView != null && mAdsContainer != null && loadingParentView == mAdsContainer) {
            return
        }
        hideLoadingView()

        mAdsContainer?.let { adContainer ->
            // Set height cho container là WrapContent
            AdsUtils.setHeightForContainer(adContainer, 0)

            adContainer.removeAllViews()
            val containerLayoutParams = adContainer.layoutParams
            containerLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            adContainer.layoutParams = containerLayoutParams
            adContainer.addView(mLoadingView)

            mLoadingView?.findViewById<ViewGroup>(R.id.container_ad_loading)?.apply {
                try {
                    val params = layoutParams
                    params.width = ScreenUtils.getScreenWidth()
                    layoutParams = params
                } catch (e: Exception) {
                    AdDebugLog.loge(e)
                }
            }
        }
    }

    private fun hideLoadingView(removeImmediate: Boolean = false) {
        mLoadingView?.let { loadingView ->
            (loadingView.parent as? ViewGroup)?.let { container ->
                try {
                    if (removeImmediate) {
                        container.removeView(loadingView)
                    } else if (getContainer() != null && container.hashCode() != getContainer()?.hashCode()) {
//                        AdDebugLog.logd("$TAG \nremoveLoadingViewFromContainer: ${getContainer()?.hashCode()}")
                        container.removeView(loadingView)
                    }
                } catch (e: Exception) {
                    AdDebugLog.loge(e)
                }
            }
        }
    }

    /**
     * Reload when Ad clicked
     * */
    private fun reloadWhenAdClicked() {
        removeAdsFromContainer()
        destroyAdInstance()
        showAd()
    }

    override fun addAdsToContainer() {
        hideLoadingView(removeImmediate = true)
        if (AdsConfig.getInstance().hasWindowFocus) {
            visibleAds()
        } else {
            invisibleAds()
        }

        val container = getContainer()
        AdsUtils.addAdsToContainer(container, mAdView)
        if (container != null) {
            if (mBottomAdHeight == 0) {
                // Cần phải chờ AdView được add vào container thì mới lấy được AdView height
                mAdView?.viewTreeObserver?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        mAdView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                        // Get AdView height
                        validateAdHeight()
                        // Set height cho container là AdView height
                        AdsUtils.setHeightForContainer(container, mBottomAdHeight)
                    }
                })
            } else {
                // Set height cho container là AdView height
                AdsUtils.setHeightForContainer(container, mBottomAdHeight)
            }
        }
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