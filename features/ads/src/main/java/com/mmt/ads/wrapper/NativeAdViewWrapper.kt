package com.mmt.ads.wrapper

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.blankj.utilcode.util.ConvertUtils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController.VideoLifecycleCallbacks
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.mmt.ads.AdsConstants
import com.mmt.ads.R
import com.mmt.ads.config.AdsConfig
import com.mmt.ads.models.NativeAdType
import com.mmt.ads.utils.AdDebugLog


class NativeAdViewWrapper(context: Context, adId: String, nativeAdType: NativeAdType = NativeAdType.SMALL) : AdWrapper(context, adId) {
    init {
        TAG = "[${this::class.java.simpleName}] ${hashCode()} -- "
    }

    private var mNativeAd: NativeAd? = null
    private var isDestroy = false

    var mLayoutType: NativeAdType = nativeAdType

    fun setLayout(nativeAdType: NativeAdType) {
        mLayoutType = nativeAdType
    }

    override fun removeAdsFromContainer() {
        getContainer()?.let { container ->
            checkAndRemoveGlobalLayoutListener()

            if (container.childCount > 0) {
                val nativeAdView = container.getChildAt(0)
                if (nativeAdView is NativeAdView) {
                    nativeAdView.destroy()
                }
            }
            container.removeAllViews()
        }
    }

    fun removeContainer(viewGroup: ViewGroup?) {
        val currentContainer = getContainer()
        if (currentContainer != null && viewGroup != null && currentContainer.hashCode() == viewGroup.hashCode()) {
            removeAdsFromContainer()
            deleteContainer()
        }
    }

    fun showAds(context: Context?, viewGroup: ViewGroup?) {
        if (!AdsConfig.getInstance().canShowAd() || (context == null && mContext == null)) return
        val appContext = if (context != null) context.applicationContext else mContext!!.applicationContext

        viewGroup?.let {
            /*val currentContainer = getContainer()
            if (currentContainer != null && currentContainer.hashCode() == viewGroup.hashCode() && currentContainer.childCount > 0) {
                // If the current container equals input container that needs to display Ads, and the Ads are displayed in it -> RETURN
                AdDebugLog.loge("RETURN when currentContainer = viewGroup AND currentContainer has child view")
                return
            }*/

            // Remove Ads from current container
            removeAdsFromContainer()
            // Keep current container instance need to show Ads
            updateContainer(viewGroup)
        }

        if (!checkConditions()) return

        isLoading = true
        isLoaded = false

        val adLoaderBuilder = AdLoader.Builder(appContext, getAdId()).forNativeAd { ad: NativeAd ->
            AdDebugLog.logi(TAG + "NativeAd loaded id ${getAdId()}")
            onAdLoaded(ad)
        }.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                AdDebugLog.loge(TAG + "\nlayoutType: " + mLayoutType + "\nerror code: " + loadAdError.code + "\nerror message: " + loadAdError.message + "\nadsId: " + mAdId)
                onAdFailedToLoad(loadAdError.code)
            }

            override fun onAdClicked() {
                super.onAdClicked()
                // Notify event
                notifyAdClicked()
                // Reload Ad
                reloadWhenAdClicked()
            }
        })

        var adSize = AdSize.MEDIUM_RECTANGLE
        var options: NativeAdOptions? = getNativeAdOptions()
        if (mLayoutType == NativeAdType.SMALL || mLayoutType == NativeAdType.LIST_AUDIO || mLayoutType == NativeAdType.LIST_VIDEO || mLayoutType == NativeAdType.SETTINGS) {
            AdDebugLog.logd("\nmLayoutType: $mLayoutType \n->adLoaderBuilder.forAdManagerAdView(AdSize.BANNER)")
            adSize = AdSize.FLUID
            options = null
        }
        adLoaderBuilder.forAdManagerAdView({ }, adSize)
        options?.let {
            adLoaderBuilder.withNativeAdOptions(it)
        }

        val adLoader: AdLoader = adLoaderBuilder.build()
        AdDebugLog.logi("Start load NativeAd id ${getAdId()}")
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun getNativeAdOptions(): NativeAdOptions {
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        return NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
    }

    private fun getAdId(): String {
        return if (AdsConfig.getInstance().isTestMode) {
            AdsConstants.native_ad_test_id
        } else {
            mAdId
        }
    }

    private fun onAdLoaded(nativeAd: NativeAd) {
        // Set flags
        isLoading = false
        isLoaded = true
        // Mark flag for this id just loaded
        AdsConfig.getInstance().onAdLoaded(mAdId)
        if (isDestroy) {
            mNativeAd?.destroy()
            nativeAd.destroy()
            return
        }
        // Inflate nativeAd
        mNativeAd = nativeAd
        showNativeAd()
        // Notify Ad loaded
        notifyAdLoaded()
    }

    private fun onAdFailedToLoad(errorCode: Int) {
        // Set flags
        isLoading = false
        isLoaded = false
        // Mark flag for this id just load failed
        AdsConfig.getInstance().onAdFailedToLoad(mAdId)
        // removeAdFromContainer
        removeAdFromContainer()
        // Notify load failed
        notifyAdLoadFailed(errorCode)
    }

    private fun reloadWhenAdClicked() {
        // Set flags
        isLoading = false
        isLoaded = false
        // Remove Ads from container
        removeAdsFromContainer()
        // Post delay to reload Ad
        mHandler.postDelayed({
            if (!isLoading) {
                destroyAdInstance()
                showAds(mContext, getContainer())
            }
        }, 1000)
    }

    private fun removeAdFromContainer() {
        getContainer()?.let { container ->
            if (container.childCount > 0) {
                val nativeAdView = container.getChildAt(0)
                if (nativeAdView is NativeAdView) {
                    nativeAdView.destroy()
                }
            }
            container.removeAllViews()
        }
    }

    private fun isAdInList(): Boolean {
        return mLayoutType == NativeAdType.LIST_AUDIO || mLayoutType == NativeAdType.LIST_VIDEO
    }

    private fun showNativeAd() {
        mNativeAd?.let {
            getContainer()?.let { container ->
                if (container.context != null) {
                    if (!container.isAttachedToWindow && !isAdInList()) {
                        // Container is generating -> wait until the view is attached to window
                        waitContainerAttachedToWindow(container)
                        return
                    }
                    AdDebugLog.logd("$TAG \nshowNativeAd with layoutType = $mLayoutType \nContainer: ${getContainer()?.hashCode()}")
                    val containerContext = container.context
                    // Set MATCH_PARENT for container
                    validateWidthForContainer(container)
                    val nativeAdView: NativeAdView
                    if (container.childCount > 0 && container.getChildAt(0) is NativeAdView) {
                        nativeAdView = container.getChildAt(0) as NativeAdView
                    } else {
                        // Create a NativeAdView with a container context and set it as the parent of the NativeAdView
                        // (to ensure the NativeAdView will be destroyed when the container is destroyed)
                        nativeAdView = LayoutInflater.from(containerContext).inflate(getLayout(), container, false) as NativeAdView

                        // Add NativeAdView to container
                        container.removeAllViews()
                        container.addView(nativeAdView)
                    }
                    if (!isAdInList()) {
                        // Set visible for container (ignore type LIST_AUDIO & LIST_VIDEO)
                        container.visibility = View.VISIBLE
                        // Listen to the container detach event to remove NativeAdView from the container
                        setupAdContainerAttachStateListener(container)
                    }

                    // Fill NativeAd data for NativeAdView
                    populateNativeAdView(it, nativeAdView)
                }
            }
        }
    }

    /**
     * Set full width for container
     * */
    private fun validateWidthForContainer(container: ViewGroup) {
        val layoutParams = container.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        container.layoutParams = layoutParams
        container.minimumWidth = ConvertUtils.dp2px(250f)
    }

    /**
     * Wait container attached to window
     * */
    private var viewTreeObserverHash = -101

    private fun waitContainerAttachedToWindow(container: ViewGroup) {
        AdDebugLog.logd("$TAG \nwaitContainerAttachedToWindow: ${getContainer()?.hashCode()}")
        viewTreeObserverHash = container.hashCode()
        container.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    private fun removeGlobalLayoutListener() {
        try {
            getContainer()?.viewTreeObserver?.removeOnGlobalLayoutListener(onGlobalLayoutListener)
        } catch (_: Exception) {
        }
    }

    private val onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        if (viewTreeObserverHash == getContainer()?.hashCode()) {
            viewTreeObserverHash = -1
            // Container is attached to window -> Show Ad
            showNativeAd()
            // Remove OnGlobalLayoutListener
            removeGlobalLayoutListener()
            getContainer()?.let {
                validateWidthForContainer(it)
            }
        }
    }

    private fun checkAndRemoveGlobalLayoutListener() {
        if (viewTreeObserverHash > 0) {
            viewTreeObserverHash = -1
            removeGlobalLayoutListener()
        }
    }

    /**
     * Populates a [NativeAd] object with data from a given
     * [NativeAd].
     *
     * @param nativeAd     the object containing the ad's assets
     * @param nativeAdView the view to be populated
     */
    private fun populateNativeAdView(nativeAd: NativeAd, nativeAdView: NativeAdView) {
        try {
            // Set the media view. Media content will be automatically populated in the media view once
            // adView.setNativeAd() is called.
            val mediaView: MediaView? = nativeAdView.findViewById(R.id.ad_media)
            mediaView?.let { nativeAdView.mediaView = mediaView }

            // Set other ad assets.
            nativeAdView.headlineView = nativeAdView.findViewById(R.id.ad_headline)
            nativeAdView.bodyView = nativeAdView.findViewById(R.id.ad_body)
            nativeAdView.callToActionView = nativeAdView.findViewById(R.id.ad_call_to_action)
            nativeAdView.iconView = nativeAdView.findViewById(R.id.ad_app_icon)
            nativeAdView.priceView = nativeAdView.findViewById(R.id.ad_price)
            nativeAdView.starRatingView = nativeAdView.findViewById(R.id.ad_stars)
            nativeAdView.storeView = nativeAdView.findViewById(R.id.ad_store)
            nativeAdView.advertiserView = nativeAdView.findViewById(R.id.ad_advertiser)
            setStyleForNativeAdView(nativeAdView)

            // The headline is guaranteed to be in every UnifiedNativeAd.
            if (nativeAdView.headlineView != null) {
                (nativeAdView.headlineView as TextView?)?.text = nativeAd.headline
            }

            // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
            // check before trying to display them.
            nativeAdView.bodyView?.let { bodyView ->
                if (nativeAd.body == null) {
                    bodyView.visibility = View.INVISIBLE
                } else {
                    bodyView.visibility = View.VISIBLE
                    (bodyView as TextView?)?.text = nativeAd.body
                }
            }
            // CTA button
            nativeAdView.callToActionView?.let { callToActionView ->
                if (nativeAd.callToAction == null) {
                    callToActionView.visibility = View.INVISIBLE
                } else {
                    callToActionView.visibility = View.VISIBLE
                    if (callToActionView is Button) {
                        callToActionView.text = nativeAd.callToAction
                    } else if (callToActionView is TextView) {
                        callToActionView.text = nativeAd.callToAction
                    }
                }
            }

            // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
            // check before trying to display them.
            nativeAdView.iconView?.let { iconView ->
                if (nativeAd.icon == null) {
                    iconView.visibility = View.GONE
                } else {
                    iconView.visibility = View.VISIBLE
                    (iconView as ImageView).setImageDrawable(nativeAd.icon!!.drawable)
                }
            }
            // Price
            nativeAdView.priceView?.let { priceView ->
                if (nativeAd.price == null) {
                    priceView.visibility = View.INVISIBLE
                } else {
                    priceView.visibility = View.VISIBLE
                    (priceView as TextView).text = nativeAd.price
                }
            }
            // Store view info
            nativeAdView.storeView?.let { storeView ->
                if (nativeAd.store == null) {
                    storeView.visibility = View.INVISIBLE
                } else {
                    storeView.visibility = View.VISIBLE
                    (storeView as TextView).text = nativeAd.store
                }
            }
            // Advertiser
            if (nativeAd.advertiser == null) {
                nativeAdView.advertiserView?.visibility = View.INVISIBLE
            } else {
                nativeAdView.advertiserView?.let { advertiserView ->
                    advertiserView.visibility = View.VISIBLE
                    (advertiserView as TextView).text = nativeAd.advertiser
                    // Gone storeView with type = SMALL
                    if (mLayoutType == NativeAdType.SMALL) {
                        nativeAdView.storeView?.visibility = View.GONE
                    }
                }

            }
            // App rating
            if (nativeAd.starRating == null) {
                nativeAdView.starRatingView?.visibility = View.INVISIBLE
            } else {
                nativeAdView.starRatingView?.let { starRatingView ->
                    (starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
                    starRatingView.setVisibility(View.VISIBLE)
                    if (mLayoutType == NativeAdType.SMALL) {
                        nativeAdView.storeView?.visibility = View.GONE
                        nativeAdView.advertiserView?.visibility = View.GONE
                    }
                }
            }

            // This method tells the Google Mobile Ads SDK that you have finished populating your
            // native ad view with this native ad. The SDK will populate the adView's MediaView
            // with the media content from this native ad.
            nativeAdView.setNativeAd(nativeAd)

            mediaView?.let {
                // Get the video controller for the ad. One will always be provided, even if the ad doesn't
                // have a video asset.
                if (nativeAd.mediaContent != null && nativeAd.mediaContent!!.hasVideoContent()) {
                    val videoController = nativeAd.mediaContent!!.videoController
                    videoController.mute(true)
                    // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
                    // VideoController will call methods on this object when events occur in the video
                    // lifecycle.
                    videoController.videoLifecycleCallbacks = object : VideoLifecycleCallbacks() {
                        override fun onVideoEnd() {
                            // Publishers should allow native ads to complete video playback before refreshing
                            // or replacing them with another ad in the same UI location.
                            super.onVideoEnd()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            AdDebugLog.loge(e)
        }
    }

    private fun setStyleForNativeAdView(adView: NativeAdView) {
        try {
            // Set color for CallToAction btn
            adView.callToActionView?.backgroundTintList = ColorStateList.valueOf(AdsConfig.getInstance().accentColor)
            adView.starRatingView?.let {
                (it as RatingBar).progressTintList = ColorStateList.valueOf(Color.parseColor("#f1b12b"))
            }
            // Set text color
            if (AdsConfig.getInstance().textSecondaryColor != -1) {
                val adBody = adView.findViewById<TextView>(R.id.ad_body)
                adBody?.setTextColor(AdsConfig.getInstance().textSecondaryColor)
            }
            if (AdsConfig.getInstance().textMainColor != -1) {
                val adHeadline = adView.findViewById<TextView>(R.id.ad_headline)
                adHeadline?.setTextColor(AdsConfig.getInstance().textMainColor)
            }
            // Set background
            if (AdsConfig.getInstance().nativeAdBackgroundColor != -1) {
                val background = adView.findViewById<View>(R.id.native_ad_background)
                if (background != null && background.background == null) {
                    background.setBackgroundColor(AdsConfig.getInstance().nativeAdBackgroundColor)
                }
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun getLayout(): Int {
        return if (mLayoutType == NativeAdType.SMALL) {
            R.layout.native_ad_bottom
        } else {
            R.layout.native_ad_medium
        }
    }

    override fun addAdsToContainer() {
        showNativeAd()
    }

    private val listener: View.OnAttachStateChangeListener = object : View.OnAttachStateChangeListener {

        override fun onViewAttachedToWindow(view: View) {}

        override fun onViewDetachedFromWindow(view: View) {
            if (view is ViewGroup && view.childCount > 0) {
                val nativeAdView = view.getChildAt(0)
                if (nativeAdView is NativeAdView) {
                    nativeAdView.destroy()
                }
                view.removeAllViews()
            }
            view.removeOnAttachStateChangeListener(this)
        }
    }

    private fun setupAdContainerAttachStateListener(container: View?) {
        container?.let {
            container.removeOnAttachStateChangeListener(listener)
            container.addOnAttachStateChangeListener(listener)
        }
    }

    override fun destroyAdInstance() {
        // Set flags
        isLoading = false
        isLoaded = false
        // Destroy Ad instance
        mNativeAd?.destroy()
        mNativeAd = null
    }

    override fun destroy() {
        isDestroy = true
    }
}