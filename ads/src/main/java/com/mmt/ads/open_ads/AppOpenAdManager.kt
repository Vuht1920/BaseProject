package com.mmt.ads.open_ads

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.mmt.ads.utils.AdDebugLog
import java.util.Date

class AppOpenAdManager(context: Context) {
    private val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(context.applicationContext)
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false

    /** Keep track of the time an app open ad is loaded to ensure you don't show an expired ad. */
    private var loadTime: Long = 0

    /**
     * Load an ad.
     *
     * @param context the context of the activity that loads the ad
     */
    fun loadAd(context: Context) {
        // Do not load ad if there is an unused ad or one is already loading.
        if (isLoadingAd || isAdAvailable()) {
            return
        }

        isLoadingAd = true
        val request = AdManagerAdRequest.Builder().build()
        AppOpenAd.load(
            context,
            AdsConfig.AD_UNIT_ID,
            request,
            object : AppOpenAdLoadCallback() {
                /**
                 * Called when an app open ad has loaded.
                 *
                 * @param ad the loaded app open ad.
                 */
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    AdDebugLog.logd("onAdLoaded.")
                    Toast.makeText(context, "onAdLoaded", Toast.LENGTH_SHORT).show()
                }

                /**
                 * Called when an app open ad has failed to load.
                 *
                 * @param loadAdError the error.
                 */
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    AdDebugLog.logd("onAdFailedToLoad: " + loadAdError.message)
                    Toast.makeText(context, "onAdFailedToLoad", Toast.LENGTH_SHORT).show()
                }
            },
        )
    }

    /** Check if ad was loaded more than n hours ago. */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /** Check if ad exists and can be shown. */
    private fun isAdAvailable(): Boolean {
        // Ad references in the app open beta will time out after four hours, but this time limit
        // may change in future beta versions. For details, see:
        // https://support.google.com/admob/answer/9341964?hl=en
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    /**
     * Show the ad if one isn't already showing.
     *
     * @param activity the activity that shows the app open ad
     */
    fun showAdIfAvailable(activity: Activity) {
        showAdIfAvailable(
            activity,
            object : OnShowOpenAdCompleteListener {
                override fun onShowOpenAdComplete() {
                    // Empty because the user will go back to the activity that shows the ad.
                }
            },
        )
    }

    /**
     * Show the ad if one isn't already showing.
     *
     * @param activity the activity that shows the app open ad
     * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
     */
    fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: OnShowOpenAdCompleteListener) {
        // If the app open ad is already showing, do not show the ad again.
        if (isShowingAd) {
            AdDebugLog.logd("The app open ad is already showing.")
            return
        }

        // If the app open ad is not available yet, invoke the callback.
        if (!isAdAvailable()) {
            AdDebugLog.logd("The app open ad is not ready yet.")
            onShowAdCompleteListener.onShowOpenAdComplete()
            if (googleMobileAdsConsentManager.canRequestAds) {
                loadAd(activity)
            }
            return
        }

        AdDebugLog.logd("Will show ad.")

        appOpenAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                /** Called when full screen content is dismissed. */
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    appOpenAd = null
                    isShowingAd = false
                    AdDebugLog.logd("onAdDismissedFullScreenContent.")
                    Toast.makeText(activity, "onAdDismissedFullScreenContent", Toast.LENGTH_SHORT).show()

                    onShowAdCompleteListener.onShowOpenAdComplete()
                    if (googleMobileAdsConsentManager.canRequestAds) {
                        loadAd(activity)
                    }
                }

                /** Called when fullscreen content failed to show. */
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    isShowingAd = false
                    AdDebugLog.logd("onAdFailedToShowFullScreenContent: " + adError.message)
                    Toast.makeText(activity, "onAdFailedToShowFullScreenContent", Toast.LENGTH_SHORT).show()

                    onShowAdCompleteListener.onShowOpenAdComplete()
                    if (googleMobileAdsConsentManager.canRequestAds) {
                        loadAd(activity)
                    }
                }

                /** Called when fullscreen content is shown. */
                override fun onAdShowedFullScreenContent() {
                    AdDebugLog.logd("onAdShowedFullScreenContent.")
                    Toast.makeText(activity, "onAdShowedFullScreenContent", Toast.LENGTH_SHORT).show()
                }
            }
        isShowingAd = true
        appOpenAd?.show(activity)
    }
}

/**
 * Interface definition for a callback to be invoked when an app open ad is complete (i.e.
 * dismissed or fails to show).
 */
interface OnShowOpenAdCompleteListener {
    fun onShowOpenAdComplete()
}