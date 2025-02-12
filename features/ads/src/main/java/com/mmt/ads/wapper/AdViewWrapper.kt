package com.mmt.ads.wapper

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.NetworkUtils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.mmt.ads.AdmobLoader
import com.mmt.ads.config.AdsConfig
import com.mmt.ads.utils.AdDebugLog
import com.mmt.ads.utils.AdsConstants
import com.mmt.ads.utils.AdsUtils


class AdViewWrapper(context: Context, adId: String) : AdWrapper(context, adId) {

    init {
        TAG = "[${this::class.java.simpleName}] ${hashCode()} -- "
    }

    private val mDefaultBottomAdHeight = ConvertUtils.dp2px(60f)
    private var mAdView: AdView? = null
    private var mLoadingView: View? = null
    private var isMediumBanner = false
    private var mBottomAdHeight = 0

    override fun getCacheTime(): Long {
        return if (AdsConfig.getInstance().isTestCacheAdsTime) 120_000 else 3600000 // 1 hour
    }

    override fun visibleAds() {
        mAdView?.visibility = View.VISIBLE
    }

    override fun invisibleAds() {
        mAdView?.visibility = View.INVISIBLE
    }

    fun showBottomBanner(container: ViewGroup? = null) {
        updateContainer(container)
        if (!checkConditions()) return
        if (!NetworkUtils.isConnected()) {
            wrapHeightForContainer()
            return
        }

        initNormalAdView()
    }

    fun showMediumBanner(container: ViewGroup? = null) {
        updateContainer(container)
        if (!checkConditions()) return
        if (!NetworkUtils.isConnected()) return

        initMediumAdView()
    }

    fun getAdId(): String {
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
        loadedTimestamp = 0

        val adId = getAdId()
        val adListener = object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                val message = if (error.message.isNotEmpty()) "\nErrorMsg: ${error.message}" else ""
                val errorMsg = "\nErrorCode: ${error.code}" + message + "\nid: $adId"
                AdDebugLog.loge("$TAG NormalAdView $errorMsg")
                mBottomAdHeight = 0
//                CacheAdsHelper.showTestNotify(mContext, "Banner", "$adId, onAdFailedToLoad, message = $message", adId.hashCode())
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
//                AdDebugLog.logd("$TAG NormalAdView loaded - mAdId: $mAdId")
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
                reloadWhenAdClicked(true)
            }
        }

//        AdDebugLog.logi(TAG + "Start load NormalAdView id $adId")
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
     * Init & show medium banner for empty screen | exit dialog
     * size: 300 x 250
     * */
    private fun initMediumAdView() {
        isMediumBanner = true
        isLoading = true
        isLoaded = false
        loadedTimestamp = 0

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
                adLoadFailed(error.code)
                // removeAdFromContainer
                removeAdsFromContainer()
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
//                AdDebugLog.logd("$TAG MediumAdView loaded - mAdId: $mAdId")
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
                reloadWhenAdClicked(false)
            }
        }

//        AdDebugLog.logi(TAG + "Start load MediumAdView id $adId")
        mContext?.let { mAdView = AdmobLoader.initMediumBanner(it.applicationContext, adId, adListener) }
    }

    /**
     * Loading view (place holder)
     * */
    override fun showLoadingView(container: ViewGroup?) {
        /*if (isMediumBanner) return // Chỉ show place holder với bottom banner

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
        }*/
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
    private fun reloadWhenAdClicked(isBottomBanner: Boolean) {
        removeAdsFromContainer()
        destroyAdInstance()
        if (isBottomBanner) {
            showBottomBanner()
        } else {
            showMediumBanner()
        }
    }

    fun detachAdFromContainerWhenKill() {
        removeAdsFromContainer()
        hideLoadingView()
        deleteContainer()
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
        if (!isMediumBanner && container != null) {
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
        (mAdView?.parent as? ViewGroup)?.let { parentContainer ->
            parentContainer.removeAllViews()
            parentContainer.setBackgroundColor(Color.TRANSPARENT)
            AdsUtils.setHeightForContainer(parentContainer, 0)
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