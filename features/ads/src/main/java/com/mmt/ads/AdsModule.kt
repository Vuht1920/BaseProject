package com.mmt.ads

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.os.SystemClock
import android.view.ViewGroup
import android.webkit.WebView
import com.blankj.utilcode.util.Utils
import com.google.android.gms.ads.MobileAds
import com.mmt.ads.config.AdsConfig
import com.mmt.ads.models.AdsId
import com.mmt.ads.models.AdsType
import com.mmt.ads.models.LoadingState
import com.mmt.ads.utils.AdDebugLog
import com.mmt.ads.wrapper.AdOPAListener
import com.mmt.ads.wrapper.AdViewWrapper
import com.mmt.ads.wrapper.AdWrapperListener
import com.mmt.ads.wrapper.InterstitialOPA
import com.mmt.ads.wrapper.RewardedAdWrapper

class AdsModule private constructor() {
    companion object {
        @JvmStatic
        private val sInstance: AdsModule by lazy { AdsModule() }

        @JvmStatic
        fun getInstance(): AdsModule {
            return sInstance
        }
    }

    private var mApplication: Application? = null

    // Banner
    private var mAdViewBottom: AdViewWrapper? = null
    private var mAdViewExitDialog: AdViewWrapper? = null
    private var mAdViewEmptyScreen: AdViewWrapper? = null

    // Interstitial
    var mInterstitialOPA: InterstitialOPA? = null

    // Rewarded
    var mRewardedAd: RewardedAdWrapper? = null

    // Configs
    private var mIgnoreDestroyStaticAd = false
    private var mLoadingState = LoadingState.NONE
    private var mSession = 0

    val context: Context?
        get() = mApplication

    fun resetInitState() {
        mLoadingState = LoadingState.NONE
    }

    fun setSession(session: Int) {
        mSession = session
    }

    private fun initializeCompleted(): Boolean {
        return mLoadingState == LoadingState.FINISHED
    }

    /**
     * Set Application Context & initialize modules
     */
    fun init(application: Application, callback: InitCallback? = null): AdsModule {
        try {
            mApplication = application
            if (mLoadingState == LoadingState.NONE) {
                mLoadingState = LoadingState.LOADING
                Thread { setWebViewDataDirectorySuffix(application) }.start()
                try {
                    val start = SystemClock.elapsedRealtime()
                    MobileAds.initialize(application) {
                        mLoadingState = LoadingState.FINISHED
                        AdDebugLog.loge("MobileAds initializationCompleted -> Take " + (SystemClock.elapsedRealtime() - start) + " ms")
                        callback?.onInitializeCompleted()
                    }
                    MobileAds.setAppMuted(true)
                    MobileAds.setAppVolume(0.0f)
                } catch (e: Exception) {
                    AdDebugLog.loge(e)
                }
            }
            Utils.init(application)
            AdsConfig.getInstance().initAdsState()

            if (initializeCompleted()) {
                callback?.onInitializeCompleted()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this@AdsModule
    }

    private fun setWebViewDataDirectorySuffix(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val processName = getProcessName(context)
                val packageName = context.packageName
                if (packageName != processName) {
                    WebView.setDataDirectorySuffix(processName!!)
                }
            }
        } catch (e: Exception) {
            AdDebugLog.loge(e)
        }
    }

    private fun getProcessName(context: Context?): String? {
        if (context == null) return null
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcessInfoList = manager.runningAppProcesses
        if (appProcessInfoList.isNotEmpty()) {
            for (processInfo in appProcessInfoList) {
                if (processInfo!!.pid == Process.myPid()) {
                    return processInfo.processName
                }
            }
        }
        return null
    }

    /**
     * Set Application Context
     */
    fun setApplication(application: Application): AdsModule {
        try {
            mApplication = application
            Utils.init(application)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this@AdsModule
    }

    /**
     * Banner
     */
    fun showBannerBottom(container: ViewGroup?) {
        if (AdsConfig.getInstance().canShowAd() && AdsConfig.getInstance().isAdEnable(AdsType.BANNER_BOTTOM)) {
            mApplication?.let {
                if (mAdViewBottom == null) {
                    mAdViewBottom = AdViewWrapper(it, AdsId.banner_bottom)
                }
                mAdViewBottom?.showBottomBanner(container)
            }
        } else {
            container?.removeAllViews()
        }
    }

    fun showBannerExitDialog(container: ViewGroup?) {
        if (AdsConfig.getInstance().canShowAd() && AdsConfig.getInstance().isAdEnable(AdsType.BANNER_EXIT_DIALOG)) {
            mApplication?.let {
                if (mAdViewExitDialog == null) {
                    mAdViewExitDialog = AdViewWrapper(it, AdsId.banner_exit_dialog)
                }
                mAdViewExitDialog?.showMediumBanner(container)
            }
        } else {
            container?.removeAllViews()
        }
    }

    fun getBannerExitDialog(context: Context?): AdViewWrapper? {
        if (AdsConfig.getInstance().canShowAd()
            && AdsConfig.getInstance().isAdEnable(AdsType.BANNER_EXIT_DIALOG)
        ) {
            context?.let {
                if (mAdViewExitDialog == null) {
                    mAdViewExitDialog = AdViewWrapper(it, AdsId.banner_exit_dialog)
                }
            }
        }
        return mAdViewExitDialog
    }

    fun showBannerEmptyScreen(container: ViewGroup?) {
        if (AdsConfig.getInstance().canShowAd() && AdsConfig.getInstance().isAdEnable(AdsType.BANNER_EMPTY_SCREEN)) {
            mApplication?.let {
                if (mAdViewEmptyScreen == null) {
                    mAdViewEmptyScreen = AdViewWrapper(it, AdsId.banner_empty_screen)
                }
                mAdViewEmptyScreen?.showMediumBanner(container)
            }
        } else {
            container?.removeAllViews()
        }
    }

    /**
     * Interstitial OPA
     * */
    fun getInterstitialOPA(context: Context, opaListener: AdOPAListener? = null, adListener: AdWrapperListener? = null): InterstitialOPA? {
        if (AdsConfig.getInstance().canShowAd() && AdsConfig.getInstance().isAdEnable(AdsType.INTERSTITIAL_OPA)) {
            if (mInterstitialOPA == null) {
                mInterstitialOPA = InterstitialOPA(context)
            }
            opaListener?.let { mInterstitialOPA?.mOPAListener = opaListener }
            mInterstitialOPA?.addListener(adListener)
            return mInterstitialOPA
        }
        return null
    }

    /**
     * Rewarded Ad
     * */
    fun getRewardedAd(context: Context, rewardedAdListener: RewardedAdWrapper.Listener? = null): RewardedAdWrapper? {
        if (AdsConfig.getInstance().canShowAd() && AdsConfig.getInstance().isAdEnable(AdsType.REWARDED_AD)) {
            if (mRewardedAd == null) {
                mRewardedAd = RewardedAdWrapper(context, AdsId.rewarded_ad)
            }
            mRewardedAd?.setRewardedAdListener(rewardedAdListener)
            return mRewardedAd
        }
        return null
    }

    /* */
    fun setIgnoreDestroyStaticAd(ignoreDestroyStaticAd: Boolean) {
        mIgnoreDestroyStaticAd = ignoreDestroyStaticAd
    }

    fun destroyAllAds() {
        mIgnoreDestroyStaticAd = false
        destroyAds(mSession)
        mInterstitialOPA?.destroy()
        mInterstitialOPA = null
    }

    fun destroyAds(session: Int) {
        if (mIgnoreDestroyStaticAd) {
            mIgnoreDestroyStaticAd = false
            return
        }
        if (mSession != session) {
            AdDebugLog.loge("RETURN destroyAds when mSession != session")
            return
        }
        // Banner bottom
        mAdViewBottom?.destroy()
        mAdViewBottom = null
        // Banner exit dialog
        mAdViewExitDialog?.destroy()
        mAdViewExitDialog = null
        // Banner empty screen
        mAdViewEmptyScreen?.destroy()
        mAdViewEmptyScreen = null
        // Rewarded Ad
        mRewardedAd?.destroy()
        mRewardedAd = null
    }

    interface InitCallback {
        fun onInitializeCompleted()
    }
}

