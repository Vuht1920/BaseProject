package com.mmt.ads

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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.FragmentUtils
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.mmt.ads.config.AdsConfig
import com.mmt.ads.models.AdsId
import com.mmt.ads.models.CountingState
import com.mmt.ads.models.LoadingState
import com.mmt.ads.utils.AdDebugLog
import com.mmt.ads.utils.AdsConstants
import com.mmt.ads.views.ProgressDialogFragment
import com.mmt.ads.wapper.AbsAdListeners
import com.mmt.ads.wapper.AdOPAListener
import java.lang.ref.WeakReference

class AppOpenAdsHelper(context: Context, opaListener: AdOPAListener? = null) : AbsAdListeners() {
    private val TAG = "[${this::class.java.simpleName}] ${hashCode()} -- "
    private val mAdId = AdsId.app_open_ads
    private val mHandler = Handler(Looper.getMainLooper())

    private var mApplicationContext: Context? = context.applicationContext
    private var mWeakActivity: WeakReference<AppCompatActivity?>? = null

    private var mAppOpenAd: AppOpenAd? = null
    private var mLoadingDialog: Dialog? = null
    private var mLoadingFragment: ProgressDialogFragment? = null
    var mOPAListener: AdOPAListener? = opaListener

    private var isShowAsOPA = false
    private var mLoadingState: LoadingState? = LoadingState.NONE
    var isShowingAd = false
    var mProgressBgResourceId: Int = 0
    var mCustomProgressView: View? = null

    private val mMinDelayTime = 500L

    /**
     * Keep track of the time an Inter ad is loaded to ensure you don't show an expired ad.
     */
    private var loadedTimestamp: Long = 0

    fun resetStates() {
        isShowAsOPA = false
        mCountingState = CountingState.NONE
    }

    /**
     * Kiểm tra xem AppOpenAd có còn khả dụng để show hay không. AppOpenAd có thể cached được 4h kể từ khi load thành công
     * */
    fun checkAvailableAndPreLoad(): Long {
        return if (AdsConfig.getInstance().canShowAd() && !isLoaded()) {
            AdDebugLog.logd(TAG + "preLoad")
            mAppOpenAd = null
            loadedTimestamp = 0
            startLoadAppOpenAd()
            0
        } else {
            SystemClock.elapsedRealtime() - loadedTimestamp
        }
    }

    fun preLoad() {
        if (AdsConfig.getInstance().canShowAd() && !isLoaded() && !isShowingAd) {
            AdDebugLog.logd(TAG + "preLoad")
            startLoadAppOpenAd()
        }
    }

    private fun startLoadAppOpenAd() {
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
        AdDebugLog.logi(TAG + "Load AppOpenAd id " + adId)
        mApplicationContext?.let {
            mLoadingState = LoadingState.LOADING
            val adRequest = AdRequest.Builder().build()
            AppOpenAd.load(it, adId, adRequest, appOpenAdLoadCallback)
        }
    }

    private val appOpenAdLoadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
        override fun onAdLoaded(appOpenAd: AppOpenAd) {
            super.onAdLoaded(appOpenAd)
            mLoadingState = LoadingState.FINISHED
            loadedTimestamp = SystemClock.elapsedRealtime()
            AdDebugLog.logi("$TAG onAdLoaded")
            // Save flag loaded
            AdsConfig.getInstance().onAdLoaded(mAdId)

            // Set instance
            mAppOpenAd = appOpenAd
            mAppOpenAd?.fullScreenContentCallback = fullScreenContentCallback

            // Notify event
            mOPAListener?.onAdOPALoaded()
            notifyAdLoaded()
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

            mAppOpenAd = null
            // Notify event
            notifyAdLoadFailed(error.code)
        }
    }

    private val fullScreenContentCallback = object : FullScreenContentCallback() {
        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
            super.onAdFailedToShowFullScreenContent(adError)
            AdDebugLog.loge("$TAG AdFailedToShow: ${adError.message}")
            // TH gọi show nhưng bị lỗi không thể hiển thị
            // WARNING: Xảy ra TH vẫn show AppOpenAd nhưng lại gọi vào onAdFailedToShow (trên các device bị chậm)
            onAdClosed()

            if (AdsConfig.getInstance().isTestGDPR) {
                Toast.makeText(mApplicationContext, "AppOpenAd FailedToShow: ${adError.message}", Toast.LENGTH_LONG).show()
            }
        }

        override fun onAdShowedFullScreenContent() {
            super.onAdShowedFullScreenContent()
            if (isShowAsOPA) {
                // Show OPA -> lưu lại timestamp để check freq time
                AdsConfig.getInstance().setLastTimeOPAShow()
            } else {
                // Show khi mở lại từ background -> lưu lại timestamp để check freq time
                AdsConfig.getInstance().saveAppOpenAdShowedTimestamp()
            }
            // Reset
            mAppOpenAd = null
            loadedTimestamp = 0
            // Notify event
            mOPAListener?.onAdOPAOpened()
            notifyAdOpened()

            if (AdsConfig.getInstance().isTestGDPR) {
                Toast.makeText(mApplicationContext, "AppOpenAd showed", Toast.LENGTH_LONG).show()
            }
        }

        override fun onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent()
            onAdClosed()

            if (AdsConfig.getInstance().isTestGDPR) {
                Toast.makeText(mApplicationContext, "AppOpenAd closed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onAdClosed() {
        hideLoading()
        // Reset flag
        isShowingAd = false
        loadedTimestamp = 0
        // Notify event
        if (isShowAsOPA) {
            isShowAsOPA = false
            mOPAListener?.onAdOPACompleted()
            mOPAListener = null
        }
        notifyAdClosed()

        // Load lại Ads cho phiên sau
        preLoad()
    }

    private fun isLoadingAds(): Boolean {
        return mLoadingState == LoadingState.LOADING
    }

    private fun getAdId(): String {
        return if (AdsConfig.getInstance().isTestMode) {
            AdsConstants.app_open_ads_test_id
        } else {
            mAdId
        }
    }

    /**
    * Show AppOpenAds as OPA
    * */
    private var mCounter: CountDownTimer? = null
    private var mCountingState: CountingState? = CountingState.NONE

    fun isCounting(): Boolean {
        return mCountingState == CountingState.COUNTING
    }

    fun initAndShowOPA(activity: AppCompatActivity) {
        AdDebugLog.loge(TAG + "initAndShowOPA")
        mApplicationContext = activity.applicationContext
        mWeakActivity = WeakReference(activity)
        registerLifecycleObserver(activity)

        // Start load Ad & counter
        startLoadAppOpenAd()
        startOPALoadingCounter(activity)
    }

    private fun startOPALoadingCounter(activity: AppCompatActivity) {
        if (mCountingState != CountingState.NONE) return

        if (!AdsConfig.getInstance().canShowOPA()) {
            AdDebugLog.loge(TAG + "RETURN counter when can't showOPA")
            onOPAFinished(activity)
            return
        }

        mCountingState = CountingState.COUNTING
        val counterTimeout = AdsConfig.getInstance().interOPAProgressDelayInMs + AdsConfig.getInstance().interOPASplashDelayInMs
        val minimumDelay = if (counterTimeout < mMinDelayTime) counterTimeout else mMinDelayTime
        val interval = 100L
        AdDebugLog.loge("$TAG\ncounterTimeout: $counterTimeout")
        mCounter = object : CountDownTimer(counterTimeout, interval) {
            override fun onTick(millisUntilFinished: Long) {
                // Check if ad OPA loaded
                val passedTime = counterTimeout - millisUntilFinished
                if (passedTime >= minimumDelay && isLoaded() && activity.lifecycle.currentState.isAtLeast(State.RESUMED)) {
                    AdDebugLog.loge("$TAG\nAppOpenAd loaded when counting -> stop counter and show immediate\npassedTime: $passedTime")
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

    private fun stopCounter() {
        mCounter?.cancel()
        mCounter = null
    }

    private fun onOPAFinished(activity: AppCompatActivity?) {
        if (mCountingState == CountingState.COUNT_FINISHED) return
        AdDebugLog.loge("onOPAFinished")
        mCountingState = CountingState.COUNT_FINISHED
        showAsOPA(activity)
        if (!isShowingAd) { // Ads not showing
            mOPAListener?.onAdOPACompleted()
        }
    }

    fun showAsOPA(activity: AppCompatActivity?): Boolean {
        try {
            activity?.let {
                if (isLoaded() && AdsConfig.getInstance().canShowOPA()) {
                    isShowingAd = true
                    isShowAsOPA = true
                    showLoading(it)
                    mAppOpenAd?.show(activity)
                    AdDebugLog.loge(TAG + "show AppOpenAd as OPA")
                    return true
                }
            }
        } catch (e: Exception) {
            AdDebugLog.loge(e)
            isShowingAd = false
            isShowAsOPA = false
            mOPAListener = null
            hideLoading()
        }
        return false
    }

    fun showWhenResume(activity: AppCompatActivity?): Boolean {
        try {
            activity?.let {
                if (isLoaded() && AdsConfig.getInstance().canShowAppOpenAd()) {
                    resetStates()
                    isShowingAd = true
//                    showLoading(it)
                    mAppOpenAd?.show(activity)
                    AdDebugLog.logi(TAG + "show AppOpenAd when resume from background")
                    return true
                }
            }
        } catch (e: Exception) {
            isShowingAd = false
            hideLoading()
            onAdClosed()
        }
        return false
    }

    fun show(activity: AppCompatActivity?): Boolean {
        try {
            activity?.let {
                if (isLoaded()) {
                    isShowingAd = true
                    showLoading(it)
                    mAppOpenAd?.show(activity)
                    AdDebugLog.logi(TAG + "show AppOpenAd")
                    return true
                }
            }
        } catch (e: Exception) {
            isShowingAd = false
            hideLoading()
        }
        return false
    }

    private fun registerLifecycleObserver(activity: AppCompatActivity?) {
        activity?.lifecycle?.addObserver(lifecycleObserver)
    }

    private val lifecycleObserver = object : DefaultLifecycleObserver {

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            mWeakActivity?.clear()
            mWeakActivity = null
            isShowAsOPA = false
            owner.lifecycle.removeObserver(this)
        }
    }

    /**
     * Check if ad exists and can be shown.
     */
    fun isLoaded(): Boolean {
        return mAppOpenAd != null && isAvailable()
    }

    /**
     * Utility method to check if ad was loaded more than 4 hours ago.
     */
    private fun isAvailable(numHours: Float = 4f): Boolean {
        val dateDifference = SystemClock.elapsedRealtime() - loadedTimestamp
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
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

    fun destroy() {
        resetStates()
        hideLoading()
        mAppOpenAd = null
        mHandler.removeCallbacksAndMessages(null)
    }

}