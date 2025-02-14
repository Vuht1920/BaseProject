package com.mmt.ads.wrapper

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.FragmentUtils
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.mmt.ads.AdsConstants
import com.mmt.ads.R
import com.mmt.ads.config.AdsConfig
import com.mmt.ads.models.AdsId
import com.mmt.ads.models.CountingState
import com.mmt.ads.models.LoadingState
import com.mmt.ads.utils.AdDebugLog
import com.mmt.ads.views.ProgressDialogFragment

class InterstitialOPA(context: Context, opaListener: AdOPAListener? = null) : AbsAdListeners() {
    private val TAG = "[${this::class.java.simpleName}] ${hashCode()} -- "
    private val mAdId = AdsId.interstitial_opa
    private val mHandler = Handler(Looper.getMainLooper())
    private val mMinDelayTime = 2000L // Sếp Hà yêu cầu show tối thiểu 2s Splash rồi mới show InterOPA

    private var mApplicationContext: Context? = context.applicationContext
    private var mActivity: AppCompatActivity? = null

    private var mInterstitialAd: InterstitialAd? = null
    private var mCounter: CountDownTimer? = null
    private var mLoadingDialog: Dialog? = null
    private var mLoadingFragment: ProgressDialogFragment? = null
    var mOPAListener: AdOPAListener? = opaListener

    var isShowingAd = false
    private var isShownOnStartUp = false
    private var mCountingState: CountingState? = CountingState.NONE
    private var mLoadingState: LoadingState? = LoadingState.NONE
    var mProgressBgResourceId: Int = 0
    var mCustomProgressView: View? = null

    /**
     * Keep track of the time an Inter ad is loaded to ensure you don't show an expired ad.
     */
    private var loadedTimestamp: Long = 0

    fun resetStates() {
        mCountingState = CountingState.NONE
        mLoadingState = LoadingState.NONE
        isShowingAd = false
        isShownOnStartUp = false
    }

    fun isCounting(): Boolean {
        return mCountingState == CountingState.COUNTING
    }

    private fun isLoadingAds(): Boolean {
        return mLoadingState == LoadingState.LOADING
    }

    fun initAndShowOPA(activity: AppCompatActivity) {
        AdDebugLog.logd(TAG + "initAndShowOPA")
        mApplicationContext = activity.applicationContext
        mActivity = activity
        registerLifecycleObserver(activity)

        // Start load Ad & counter
        startLoadInterstitial()
        startOPALoadingCounter(activity)
    }

    fun preLoad() {
        if (AdsConfig.getInstance().canShowAd() && !isLoaded() && !isShowingAd) {
            AdDebugLog.logd(TAG + "preLoad")
            startLoadInterstitial()
        }
    }

    private fun getAdId(): String {
        return if (AdsConfig.getInstance().isTestMode) {
            AdsConstants.interstitial_test_id
        } else {
            mAdId
        }
    }

    private fun startLoadInterstitial() {
        if (isLoaded()) {
            AdDebugLog.logi(TAG + "RETURN when Ads isLoaded")
            return
        }
        if (isShowingAd) {
            AdDebugLog.logi(TAG + "RETURN when Ads isShowing")
            return
        }
        if (isLoadingAds()) {
            AdDebugLog.logi(TAG + "RETURN when Ads isLoading")
            return
        }
        val adId = getAdId()
        if (AdsConfig.getInstance().cantLoadId(adId)) {
            AdDebugLog.loge("$TAG RETURN because this id just failed to load\nid: $adId")
            return
        }

        // Init Ads
        AdDebugLog.loge(TAG + "Load Inter OPA id " + adId)
        mApplicationContext?.let {
            mLoadingState = LoadingState.LOADING
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(it, adId, adRequest, interstitialAdLoadCallback)
        }
    }

    private val interstitialAdLoadCallback = object : InterstitialAdLoadCallback() {
        override fun onAdLoaded(interstitialAd: InterstitialAd) {
            super.onAdLoaded(interstitialAd)
            mLoadingState = LoadingState.FINISHED
            loadedTimestamp = SystemClock.elapsedRealtime()
            AdDebugLog.loge("$TAG onAdLoaded:\nisCounting: ${isCounting()}, activityState: ${mActivity?.lifecycle?.currentState}" )
            // Save flag loaded
            AdsConfig.getInstance().onAdLoaded(mAdId)

            // Set instance
            mInterstitialAd = interstitialAd
            mInterstitialAd?.fullScreenContentCallback = fullScreenContentCallback

            // Notify event
            mOPAListener?.onAdOPALoaded()
            notifyAdLoaded()

            // Check flow counter OPA
            if (isCounting() && mActivity?.lifecycle?.currentState?.isAtLeast(State.STARTED) == true) {
                onOPAFinished(mActivity)
            }
        }

        override fun onAdFailedToLoad(error: LoadAdError) {
            super.onAdFailedToLoad(error)
            mLoadingState = LoadingState.FINISHED
            loadedTimestamp = 0
            val message = if (error.message.isNotEmpty()) "\nErrorMsg: ${error.message}" else ""
            val errorMsg = "\nErrorCode: ${error.code}" + message + "\nid: $mAdId"
            AdDebugLog.loge("$TAG onAdFailedToLoad: $errorMsg")
            // Save flag load failed
            AdsConfig.getInstance().onAdFailedToLoad(mAdId)

            mInterstitialAd = null
            // Notify event
            notifyAdLoadFailed(error.code)
            // Check flow counter OPA
            if (isCounting()) {
                stopCounter()
                onOPAFinished(mActivity)
            }
        }
    }

    private val fullScreenContentCallback = object : FullScreenContentCallback() {
        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
            super.onAdFailedToShowFullScreenContent(adError)
            // TH gọi show nhưng bị lỗi không thể hiển thị
            onAdClosed()
        }

        override fun onAdShowedFullScreenContent() {
            super.onAdShowedFullScreenContent()
            if (isShownOnStartUp) {
                // Show OPA -> lưu lại timestamp để check freq time
                AdsConfig.getInstance().setLastTimeOPAShow()
            }
            // Reset
            mInterstitialAd = null
            loadedTimestamp = 0
            // Notify event
            mOPAListener?.onAdOPAOpened()
            notifyAdOpened()
        }

        override fun onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent()
            onAdClosed()
        }
    }

    private fun onAdClosed() {
        hideLoading()
        // Reset flag
        isShowingAd = false
        loadedTimestamp = 0
        // Notify event
        if (isShownOnStartUp) {
            isShownOnStartUp = false
            mOPAListener?.onAdOPACompleted()
        }
        notifyAdClosed()

        // Load lại Ads cho phiên sau
        preLoad()
    }

    private fun startOPALoadingCounter(activity: AppCompatActivity) {
        if (mCountingState != CountingState.NONE) return

        isShownOnStartUp = false
        if (!AdsConfig.getInstance().canShowOPA()) {
            AdDebugLog.loge(TAG + "RETURN counter when can't showOPA")
            onOPAFinished(activity)
            return
        }

        mCountingState = CountingState.COUNTING
        val counterTimeout = AdsConfig.getInstance().interOPAProgressDelayInMs + AdsConfig.getInstance().splashDelayInMs
        val minimumDelay = if (counterTimeout < mMinDelayTime) counterTimeout else mMinDelayTime
        val interval = 100L
        AdDebugLog.loge("$TAG\ncounterTimeout: $counterTimeout")
        mCounter = object : CountDownTimer(counterTimeout, interval) {
            override fun onTick(millisUntilFinished: Long) {
                // Check if ad OPA loaded
                val passedTime = counterTimeout - millisUntilFinished
                if (passedTime >= minimumDelay && mInterstitialAd != null && activity.lifecycle.currentState.isAtLeast(State.RESUMED)) {
                    AdDebugLog.loge("$TAG\nInterstitial loaded when counting -> stop counter and show immediate\npassedTime: $passedTime")
                    stopCounter()
                    onOPAFinished(activity)
                }
            }

            override fun onFinish() {
                AdDebugLog.logd("$TAG\nCounter FINISHED")
                onOPAFinished(activity)
            }
        }
        mCounter?.start()
    }

    private fun onOPAFinished(activity: AppCompatActivity?) {
        if (mCountingState == CountingState.COUNT_FINISHED) return

        AdDebugLog.loge("onOPAFinished")
        mCountingState = CountingState.COUNT_FINISHED
        isShownOnStartUp = show(activity)
        if (!isShowingAd) { // Ads not showing
            mOPAListener?.onAdOPACompleted()
            hideLoading()
        }
    }

    private fun registerLifecycleObserver(activity: AppCompatActivity?) {
        activity?.lifecycle?.addObserver(lifecycleObserver)
    }

    private val lifecycleObserver = object : DefaultLifecycleObserver {

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            owner.lifecycle.removeObserver(this)
            stopCounter()
            if (mActivity?.lifecycle?.hashCode() == owner.lifecycle.hashCode()) {
                mActivity = null
            }
        }
    }

    /**
     * Check if ad exists and can be shown.
     */
    fun isLoaded(): Boolean {
        return mInterstitialAd != null && isAvailable()
    }

    /**
     * Utility method to check if ad was loaded more than 1 hours ago.
     */
    private fun isAvailable(): Boolean {
        val dateDifference = SystemClock.elapsedRealtime() - loadedTimestamp
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour
    }

    fun show(activity: AppCompatActivity?): Boolean {
        try {
            activity?.let {
                AdDebugLog.logi(TAG + "isLoaded: ${isLoaded()}\ncanShowOPA: ${AdsConfig.getInstance().canShowOPA()}\nlifecycle currentState: ${it.lifecycle.currentState}")
                if (isLoaded() && AdsConfig.getInstance().canShowOPA() && it.lifecycle.currentState.isAtLeast(State.INITIALIZED)) {
                    isShowingAd = true
                    showLoading(it)
                    mInterstitialAd?.show(activity)
                    AdDebugLog.logi(TAG + "show InterstitialOpenApp")
                    return true
                }
            }
        } catch (e: Exception) {
            isShowingAd = false
            hideLoading()
        }
        return false
    }

    @SuppressLint("InflateParams")
    fun showLoading(activity: AppCompatActivity) {
        try {
            if (mLoadingDialog?.isShowing == true) return
            if (mLoadingFragment?.isAdded == true) return

            if (mProgressBgResourceId == 0 && mCustomProgressView == null) {
                mLoadingDialog = ProgressDialog(activity).apply {
                    setTitle(activity.getString(R.string.msg_dialog_please_wait))
                    setMessage(activity.getString(R.string.msg_dialog_loading_data))
                    setCancelable(false)
                    show()
                }
                return
            }

            if (mCustomProgressView == null) {
                mCustomProgressView = activity.layoutInflater.inflate(R.layout.progress_layout, null)
                mCustomProgressView?.findViewById<ImageView>(R.id.iv_background)?.apply {
                    setImageResource(if (mProgressBgResourceId != 0) mProgressBgResourceId else R.drawable.bg_black_alpha_corner)
                    alpha = 0.93f
                }
            } else if (mCustomProgressView?.parent is ViewGroup) {
                (mCustomProgressView?.parent as ViewGroup).removeView(mCustomProgressView)
            }

            mLoadingFragment = ProgressDialogFragment(mCustomProgressView)
            FragmentUtils.add(activity.supportFragmentManager, mLoadingFragment!!, android.R.id.content, false, false)
        } catch (e: Exception) {
            AdDebugLog.loge(e)
        }
    }

    private fun hideLoading() {
        if (mLoadingDialog?.isShowing == true) {
            mLoadingDialog?.dismiss()
        }
        mLoadingDialog = null

        if (mLoadingFragment?.isVisible == true) {
            mLoadingFragment?.dismiss()
        }
        mLoadingFragment = null
    }

    private fun stopCounter() {
        mCounter?.cancel()
        mCounter = null
    }

    fun destroy() {
        stopCounter()
        resetStates()
        hideLoading()
        mInterstitialAd = null
        mHandler.removeCallbacksAndMessages(null)
    }
}