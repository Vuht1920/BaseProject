package com.mmt.ads

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.mmt.ads.utils.Utils

object AdmobLoader {
    private fun buildAdRequest(): AdRequest {
        val builder = AdRequest.Builder()
        return builder.build()
    }

    /*
     * AdView
     * */
    fun initNormalBanner(context: Context?, adsId: String, adListener: AdListener?): AdView? {
        if (context == null) {
            return null
        }
        val adView = AdView(context.applicationContext)
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId = adsId
        adListener?.let { adView.adListener = it }
        adView.loadAd(buildAdRequest())
        return adView
    }

    /*
     * AdView
     * */
    fun initAdaptiveBanner(context: Context?, adsId: String?, adListener: AdListener?): AdView? {
        if (context == null) {
            return null
        }
        val adView = AdView(context.applicationContext)
        adView.setAdSize(getAdSize(context))
        adView.adUnitId = adsId!!
        if (adListener != null) {
            adView.adListener = adListener
        }
        adView.loadAd(buildAdRequest())
        return adView
    }

    fun initMediumBanner(context: Context?, adsId: String?, adListener: AdListener?): AdView? {
        if (context == null) {
            return null
        }
        val adView = AdView(context)
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE) // 300x250
        adView.adUnitId = adsId!!
        if (adListener != null) {
            adView.adListener = adListener
        }
        adView.loadAd(buildAdRequest())
        return adView
    }

    /*
     * Adaptive Banner
     * */
    private fun getAdSize(context: Context?): AdSize {
        if (context == null) {
            return AdSize.BANNER
        }

        try {// Determine the screen width (less decorations) to use for the ad width.
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            display?.let {
                val adWidth = Utils.getScreenWidth(context)
                return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
            }
        } catch (_: Exception) {
        }
        return AdSize.BANNER
    }
}
