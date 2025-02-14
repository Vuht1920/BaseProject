package com.mmt.ads.wrapper

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.mmt.ads.AdsConstants
import com.mmt.ads.GoogleConsentManager
import com.mmt.ads.R
import com.mmt.ads.config.AdsConfig
import com.mmt.ads.utils.AdDebugLog

class RewardedAdWrapper(context: Context, adId: String) : AdWrapper(context, adId) {
    private var mRewardedAd: RewardedAd? = null
    private var mProgressDialog: ProgressDialog? = null
    private var showWhenLoaded = false
    private var mListener: Listener? = null

    fun setRewardedAdListener(listener: Listener?) {
        mListener = listener
    }

    fun cancelAutoShow() {
        showWhenLoaded = false
    }

    fun preloadRewardedAd(activity: Activity) {
        if (!checkConditions()) return

        initAdMobRewardedAd(activity, false)
    }

    fun loadAndShow(activity: Activity) {
        if (!AdsConfig.getInstance().canShowAd()) {
            AdDebugLog.loge(
                "notifyAdLoadFailed when canShowAd = false " +
                        "\nisFullVersion: ${AdsConfig.getInstance().isFullVersion} " +
                        "\ncanRequestAds: ${GoogleConsentManager.getInstance(activity).canRequestAds()}"
            )
            notifyAdLoadFailed(-1)
            return
        }
        if (isLoading) {
            showWhenLoaded = true
            return
        }
        if (isLoaded) {
            showAd(activity)
            return
        }
        showProgressDialog(activity)
        initAdMobRewardedAd(activity, true)
    }

    private fun getAdId(): String {
        return if (AdsConfig.getInstance().isTestMode) {
            AdsConstants.rewarded_ad_test_id
        } else {
            mAdId
        }
    }

    private fun initAdMobRewardedAd(activity: Activity, autoShow: Boolean) {
        showWhenLoaded = autoShow
        val fullScreenContentCallback: FullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                super.onAdClicked()
                notifyAdClicked()
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                mRewardedAd = null
                dismissLoadingProgress()
                notifyAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                super.onAdFailedToShowFullScreenContent(adError)
                AdDebugLog.loge("onAdFailedToShowFullScreenContent: " + adError.message)
                dismissLoadingProgress()
                mListener?.onAdFailedToShow(adError)
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                dismissLoadingProgress()
                notifyAdOpened()
            }
        }

        val adId = getAdId()
        val rewardedAdLoadCallback: RewardedAdLoadCallback = object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                mRewardedAd = null
                val message = if (error.message.isNotEmpty()) "\nErrorMsg: ${error.message}" else ""
                val errorMsg = "\nErrorCode: ${error.code}" + message + "\nid: $adId"
                AdDebugLog.loge("$TAG $errorMsg")

                // Notify load failed
                notifyAdLoadFailed(error.code)
                // Dismiss loading
                dismissLoadingProgress()
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                AdDebugLog.logd("\n---\n[RewardedAd - onAdLoaded] adsId: $adId\n---")
                mRewardedAd = rewardedAd.apply {
                    this.fullScreenContentCallback = fullScreenContentCallback
                }

                // Notify Ad loaded
                notifyAdLoaded()
                // Show immediate if needed
                if (showWhenLoaded) showAd(activity)
            }
        }

        // Init Ads
        isLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(activity, adId, adRequest, rewardedAdLoadCallback)
    }

    fun showAd(activity: Activity?): Boolean {
        if (activity == null) {
            dismissLoadingProgress()
            return false
        }
        if (mRewardedAd != null) {
            mRewardedAd!!.show(activity) { rewardItem: RewardItem -> mListener?.onUserEarnedReward(rewardItem) }
            return true
        }
        dismissLoadingProgress()
        return false
    }

    fun dismissLoadingProgress() {
        try {
            if (mProgressDialog != null && mProgressDialog!!.isShowing) {
                mProgressDialog!!.dismiss()
                mProgressDialog = null
            }
        } catch (ignored: Exception) {
        }
    }

    private fun showProgressDialog(activity: Activity) {
        try {
            if (mProgressDialog != null && mProgressDialog!!.isShowing) {
                return
            }
            mProgressDialog = ProgressDialog(activity)
            mProgressDialog!!.setTitle(activity.getString(R.string.msg_dialog_please_wait))
            mProgressDialog!!.setMessage(activity.getString(R.string.msg_dialog_loading_ads))
            mProgressDialog!!.setCancelable(false)
            mProgressDialog!!.setCanceledOnTouchOutside(false)
            mProgressDialog!!.show()
        } catch (ignored: Exception) {
        }
    }

    override fun destroyAdInstance() {
        if (mRewardedAd != null) {
            mRewardedAd = null
        }
    }

    override fun addAdsToContainer() {
    }

    interface Listener {
        fun onUserEarnedReward(rewardItem: RewardItem)
        fun onAdFailedToShow(adError: AdError?)
    }
}
