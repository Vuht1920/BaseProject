package com.mmt.ads.config

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.os.SystemClock
import com.mmt.ads.GoogleConsentManager
import com.mmt.ads.data.repository.dataStore.PrefRepository
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.DEFAULT_INTER_OPA_PROGRESS_DELAY_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.DEFAULT_INTER_OPA_SPLASH_DELAY_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.DEFAULT_WAITING_TIME_WHEN_LOAD_FAILED_IN_MS
import com.mmt.ads.models.AdsType
import com.mmt.ads.utils.AdDebugLog
import org.json.JSONObject

@Suppress("FunctionName")
class AdsConfig {
    companion object {

        private val sInstance: AdsConfig by lazy {
            AdsConfig()
        }

        fun getInstance(): AdsConfig {
            return sInstance
        }
    }


    private var mApplication: Application? = null

    /**
     * Ad id load failed timestamp
     * */
    private val mAdLoadFailedTimestamp = HashMap<String, Long>()

    /**
     * Ad type enable state
     * key = {@link [com.tohsoft.ads.models.AdsType]
     * */
    private val mAdsEnableState = HashMap<String, Boolean>()

    private var waitingTimeWhenLoadFailedInMs = DEFAULT_WAITING_TIME_WHEN_LOAD_FAILED_IN_MS
    var isFullVersion = false
    var hasWindowFocus = true
    var isTestMode = false
    var isTestGDPR = false
    var isCacheAds = false
    var isTestCacheAdsTime = false
    var freqAppOpenAdInMs: Long = 0

    private val prefRepository by lazy { PrefRepository.getInstance(mApplication!!) }


    /**
     * NativeAd theme color config
     * */
    var accentColor = Color.parseColor("#4967F9")
    var textMainColor = -1
    var textSecondaryColor = -1
    var nativeAdBackgroundColor = -1

    fun mustInit(): Boolean {
        return mApplication == null
    }

    // Init
    fun init(application: Application?): AdsConfig {
        mApplication = application
        freqAppOpenAdInMs = prefRepository.freqCapAppOpenAdInMs
        waitingTimeWhenLoadFailedInMs = prefRepository.waitingTimeWhenLoadFailedInMs
        return this@AdsConfig
    }

    fun initAdsState(context: Context): AdsConfig {
        if (mAdsEnableState.isEmpty()) {
            val data = prefRepository.adsEnableState
            setAdsEnableState(data)
        }
        return this@AdsConfig
    }

    fun canShowAd(): Boolean {
        return !isFullVersion && mApplication != null && GoogleConsentManager.getInstance(mApplication!!).canRequestAds()
    }

    fun getReasonCantShowAds(): String {
        var reason = ""
        if (isFullVersion) {
            reason = "FullVersion"
        } else if (mApplication == null) {
            reason = "ApplicationNULL"
        } else if (!GoogleConsentManager.getInstance(mApplication!!).canRequestAds()) {
            reason = "GDPR"
        }
        return reason
    }

    /**
     * Load success/failed timestamp của một id cụ thể
     * Chức năng giới hạn thời gian để fix lỗi load liên tục một id bị lỗi
     * */
    fun onAdFailedToLoad(adId: String?) {
        if (adId != null) {
            mAdLoadFailedTimestamp[adId] = SystemClock.elapsedRealtime()
        }
    }

    fun onAdLoaded(adId: String?) {
        if (adId != null) {
            mAdLoadFailedTimestamp.remove(adId)
        }
    }

    /**
     * Kiểm tra xem AD id có thể load hay không, nếu id này đã load failed trước đó thì phải cách ít nhất WAITING_TIME_WHEN_LOAD_FAILED thì mới load lại
     */
    fun cantLoadId(adId: String): Boolean {
        var timestamp: Long = 0
        if (mAdLoadFailedTimestamp.containsKey(adId)) {
            timestamp = mAdLoadFailedTimestamp[adId]!!
        }
        return SystemClock.elapsedRealtime() - timestamp < waitingTimeWhenLoadFailedInMs
    }

    fun parseAdsStateConfig(jsonData: String?): LinkedHashMap<String, Boolean> {
        if (!jsonData.isNullOrEmpty()) {
            try {
                // Parse config
                val states = LinkedHashMap<String, Boolean>()
                val jsonObject = JSONObject(jsonData)
                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    states[key] = jsonObject.getBoolean(key)
                }
                return states
            } catch (e: Exception) {
                AdDebugLog.loge(e)
            }
        }
        return LinkedHashMap()
    }

    /**
     * jsonData is JSONObject, it will like this below
     * .
     * {
     * "banner_bottom" : true,
     * "native_bottom_main" : true,
     * "native_bottom_other" : true
     * }
     */
    fun setAdsEnableState(jsonData: String?): AdsConfig {
        try {
            if (!jsonData.isNullOrEmpty()) {
                // Save config to pref
                prefRepository.setAdsEnableState(jsonData)
                val states: Map<String, Boolean> = parseAdsStateConfig(jsonData)
                mAdsEnableState.clear()
                mAdsEnableState.putAll(states)
            }
        } catch (e: Exception) {
            AdDebugLog.loge(e)
        }
        return this@AdsConfig
    }

    fun _setWaitingTimeWhenLoadFailedInMs(timeInMs: Long): AdsConfig {
        waitingTimeWhenLoadFailedInMs = timeInMs
        prefRepository.setWaitingTimeWhenLoadFailedInMs(timeInMs)
        return this@AdsConfig
    }

    fun _setTestMode(testMode: Boolean): AdsConfig {
        isTestMode = testMode
        return this@AdsConfig
    }

    fun _setTestGDPR(testGDPR: Boolean): AdsConfig {
        isTestGDPR = testGDPR
        return this@AdsConfig
    }

    fun _setShowLog(showLog: Boolean): AdsConfig {
        AdDebugLog.DEBUG_LOG = showLog
        return this@AdsConfig
    }

    fun _setFullVersion(fullVersion: Boolean): AdsConfig {
        isFullVersion = fullVersion
        return this@AdsConfig
    }

    fun _setCacheAd(cacheAd: Boolean): AdsConfig {
        this.isCacheAds = cacheAd
        prefRepository.setFreqCapAppOpenAdInMs(freqAppOpenAdInMs)
        return this@AdsConfig
    }

    /*
     * InterOPA progress delay time
     * */
    fun setInterOPAProgressDelayInMs(time: Long): AdsConfig {
        if (mApplication != null) {
            prefRepository.setInterOPAProgressDelayInMs(time)
        }
        return this@AdsConfig
    }

    /*
     * InterOPA Splash delay time
     * */
    fun setInterOPASplashDelayInMs(time: Long): AdsConfig {
        if (mApplication != null) {
            prefRepository.setInterOPASplashDelayInMs(time)
        }
        return this@AdsConfig
    }

    /*
     * Freq time to show Interstitial Ads
     * */
    fun setInterFreqInMs(time: Long): AdsConfig {
        if (mApplication != null) {
            prefRepository.setFreqCapInterInMs(time)
        }
        return this@AdsConfig
    }

    /*
     * Frequency time limited for OPA
     * */
    fun setFreqInterOPAInMs(time: Long): AdsConfig {
        if (mApplication != null) {
            prefRepository.setFreqInterOPAInMilliseconds(time)
        }
        return this@AdsConfig
    }

    /*
     * Last time OPA showed
     * */
    fun saveInterOPAShowedTimestamp(): AdsConfig {
        if (mApplication != null) {
            prefRepository.setLastTimeOPAShowTimeStamp(SystemClock.elapsedRealtime())
        }
        return this@AdsConfig
    }

    /*
     * Minimum time to show Interstitial Ads from open app time
     * */
    fun setInterMinimumShowTimeInMs(time: Long): AdsConfig {
        if (mApplication != null) {
            prefRepository.setWaitingTimeWhenLoadFailedInMs(time)
        }
        return this@AdsConfig
    }

    /*
     *
     * */
    fun isAdEnable(type: AdsType): Boolean {
        var isEnable = false
        if (mAdsEnableState.containsKey(type.value)) {
            val enable = mAdsEnableState[type.value]
            isEnable = enable != null && enable
        }
//        AdDebugLog.logd("AdsType ${type.value}: isAdEnable = $isEnable")
        return isEnable
    }

    /*
     * This method will check condition time with config FREQ_INTER_OPA_IN_MILLISECONDS
     *
     * return true if current time minus the latest time OPA displayed > FREQ_INTER_OPA_IN_MILLISECONDS has been set
     * */
    fun canShowOPA(): Boolean {
        val freqInterOPAInMilliseconds = prefRepository.freqInterOPAInMilliseconds
        if (freqInterOPAInMilliseconds == 0L) {
            return true
        }
        val lastTimeOPAShow = prefRepository.interstitialShowedTimestamp
        return SystemClock.elapsedRealtime() - lastTimeOPAShow >= freqInterOPAInMilliseconds
    }

    /*
    * Splash delay time
    * */
    val interOPASplashDelayInMs: Long
        get() = if (mApplication != null) {
            prefRepository.interOPASplashDelayInMs
        } else DEFAULT_INTER_OPA_SPLASH_DELAY_IN_MS

    /*
    * Fake progress delay time
    * */
    val interOPAProgressDelayInMs: Long
        get() = if (mApplication != null) {
            prefRepository.interOPAProgressDelayInMs
        } else DEFAULT_INTER_OPA_PROGRESS_DELAY_IN_MS

    /*
     *
     * */
    fun saveInterstitialShowedTimestamp() {
        if (mApplication != null) {
            prefRepository.setInterstitialShowedTimestamp(SystemClock.elapsedRealtime())
        }
    }

    private val interstitialShowedTimestamp: Long
        get() = if (mApplication != null) {
            prefRepository.interstitialShowedTimestamp
        } else 0L

    fun canShowInterstitial(): Boolean {
        if (mApplication != null) {
            val timePassed = SystemClock.elapsedRealtime() - interstitialShowedTimestamp
            return timePassed >= prefRepository.freqCapInterInMs
        }
        return false
    }

    /*
     * AppOpenAd show when resume to foreground
     * */
    fun saveAppOpenAdShowedTimestamp() {
        if (mApplication != null) {
            prefRepository.setAppOpenAdShowedTimeStampKey(SystemClock.elapsedRealtime())
        }
    }

    private val appOpenAdShowedTimestamp: Long
        get() = if (mApplication != null) {
            prefRepository.appOpenAdShowedTimeStamp
        } else 0L

    fun canShowAppOpenAd(): Boolean {
        if (mApplication != null) {
            val timePassed = SystemClock.elapsedRealtime() - appOpenAdShowedTimestamp
            return timePassed >= freqAppOpenAdInMs
        }
        return false
    }
}