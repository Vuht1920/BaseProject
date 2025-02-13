package com.mmt.ads.wapper

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import com.mmt.ads.config.AdsConfig
import com.mmt.ads.models.AdsId
import com.mmt.ads.models.NativeAdType


class AdsInListHelper(private val context: Context, private val adsInstances: Int = MAX_ITEMS) {

    companion object {
        const val MAX_ITEMS = 1
    }

    private val mHandler = Handler(Looper.getMainLooper())
    private val mMapAds: HashMap<Int, NativeAdViewWrapper?> = hashMapOf()
    private var mPosition = 0

    fun onWindowFocusChanged() {
        if (AdsConfig.getInstance().hasWindowFocus) {
            mMapAds.values.forEach { it?.visibleAds() }
        } else {
            mMapAds.values.forEach { it?.invisibleAds() }
        }
    }

    /*
    * Init lại số lượng ADs instances
    * */
    fun refreshAdsInstance(adsInstances: Int) {
        val size = mMapAds.size
        if (size < adsInstances) {
            for (i in size until adsInstances) {
                if (mMapAds[i] == null) {
                    mMapAds[i] = newNativeAdView()
                }
            }
        }
    }

    fun initAllAds() {
        for (i in 0 until adsInstances) {
            mMapAds[i] = newNativeAdView()
        }
    }

    fun showAds(container: ViewGroup, nativeAdType: NativeAdType?) {
        if (mPosition >= mMapAds.size) mPosition = 0
        if (mMapAds.isEmpty()) return

        var nativeAd = mMapAds[mPosition]
        if (nativeAd == null) {
            nativeAd = newNativeAdView()
            mMapAds[mPosition] = nativeAd
        }
        container.removeAllViews()
        nativeAd.showAds(context, container, nativeAdType)
        mPosition++
    }

    fun detachAdFromContainerWhenKill() {
        mHandler.removeCallbacksAndMessages(null)
        mMapAds.values.forEach { nativeAd ->
            nativeAd?.detachAdFromContainerWhenKill()
        }
        mPosition = 0
    }

    fun onDestroy() {
        mHandler.removeCallbacksAndMessages(null)
        mMapAds.values.forEach { nativeAd ->
            nativeAd?.destroy()
        }
        mMapAds.clear()
        mPosition = 0
    }

    private fun newNativeAdView() : NativeAdViewWrapper {
        return NativeAdViewWrapper(context, AdsId.native_in_list_2)
    }
}