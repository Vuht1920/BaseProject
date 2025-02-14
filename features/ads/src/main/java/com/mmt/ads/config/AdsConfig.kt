package com.mmt.ads.config

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.os.SystemClock
import com.mmt.ads.GoogleConsentManager
import com.mmt.ads.data.repository.dataStore.PrefRepository
import com.mmt.ads.models.AdsType
import com.mmt.ads.utils.AdDebugLog
import org.json.JSONObject

class AdsConfig {
    companion object {
        private const val LAST_TIME_APP_OPEN_AD_SHOWED = "last_time_app_open_ad_showed"
        private const val LAST_TIME_INTER_OPA_SHOWED = "last_time_interstitial_opa_showed"
        private const val INTERSTITIAL_SHOWED_TIMESTAMP = "interstitial_showed_timestamp"
        private const val FREQ_INTER_OPA_IN_MILLISECONDS = "freq_interstitial_opa_in_ms"
        private const val SPLASH_DELAY_IN_MS = "splash_delay_in_ms"
        private const val INTER_OPA_PROGRESS_DELAY_IN_MS = "inter_opa_progress_delay_in_ms"
        private const val FREQ_CAP_INTER_IN_MS = "freq_cap_inter_in_ms"
        private const val MINIMUM_TIME_SHOW_INTER_IN_MS = "minimum_time_show_inter_in_ms"
        private const val ADS_ENABLE_STATE = "ads_enable_state"
        private const val WAITING_TIME_WHEN_LOAD_FAILED = "waiting_time_when_load_failed"
        private const val FREQ_CAP_APP_OPEN_AD_IN_MS = "freq_cap_app_open_ad_in_ms"

        private const val DEFAULT_FREQ_CAP_APP_OPEN_AD_IN_MS = (5 * 60 * 1000).toLong()// 5 minutes
        private const val DEFAULT_FREQ_CAP_INTER_OPA_IN_MS = (15 * 60 * 1000).toLong()// 15 minutes
        private const val DEFAULT_SPLASH_DELAY_IN_MS: Long = 3000 // 3 seconds
        private const val DEFAULT_INTER_OPA_PROGRESS_DELAY_IN_MS: Long = 2000 // 2 seconds
        private const val DEFAULT_FREQ_CAP_INTER_IN_MS = (15 * 60 * 1000).toLong() // 15 minutes
        private const val DEFAULT_WAITING_TIME_WHEN_LOAD_FAILED_IN_MS = (5 * 1000).toLong()  // 5s

        private val sInstance: AdsConfig by lazy {
            AdsConfig()
        }

        @JvmStatic
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
    var isTestMode = false
    var isTestGDPR = false
    var freqAppOpenAdInMs: Long = 0
    var interstitialLoadedTimestamp: Long = 0

    /**
     * NativeAd theme color config
     * */
    var accentColor = Color.parseColor("#4967F9")
    var textMainColor = -1
    var textSecondaryColor = -1
    var nativeAdBackgroundColor = -1

    private val prefRepository by lazy { PrefRepository.getInstance(mApplication!!) }

    // Init
    fun init(application: Application?): AdsConfig {
        mApplication = application
        freqAppOpenAdInMs = prefRepository.freqCapAppOpenAdInMs
        waitingTimeWhenLoadFailedInMs = prefRepository.waitingTimeWhenLoadFailedInMs
        return this@AdsConfig
    }

    fun initAdsState(context: Context): AdsConfig {
        if (mAdsEnableState.isEmpty()) {
            setAdsEnableState(prefRepository.adsEnableState)
        }
        return this@AdsConfig
    }

    fun canShowAd(): Boolean {
        return !isFullVersion && mApplication != null && GoogleConsentManager.getInstance(mApplication!!).canRequestAds()
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

    fun setWaitingTimeWhenLoadFailed(timeInMs: Long): AdsConfig {
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

    fun setShowLog(showLog: Boolean): AdsConfig {
        AdDebugLog.DEBUG_LOG = showLog
        return this@AdsConfig
    }

    fun _setFullVersion(fullVersion: Boolean): AdsConfig {
        isFullVersion = fullVersion
        return this@AdsConfig
    }

    fun _setFreqAppOpenAdInMs(freqAppOpenAdInMs: Long): AdsConfig {
        this.freqAppOpenAdInMs = freqAppOpenAdInMs
        prefRepository.setFreqCapAppOpenAdInMs(freqAppOpenAdInMs)
        return this@AdsConfig
    }

    /*
     * Fake progress delay time
     * */
    fun setInterOPAProgressDelayInMs(time: Long): AdsConfig {
        if (mApplication != null) {
            prefRepository.setInterOPAProgressDelayInMs(time)
        }
        return this@AdsConfig
    }

    /*
     * Splash delay time
     * */
    fun setSplashDelayInMs(time: Long): AdsConfig {
        if (mApplication != null) {
            prefRepository.setSplashDelayInMs(time)
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
            prefRepository.setFreqCapInterInMs(time)
        }
        return this@AdsConfig
    }

    /*
     * Last time OPA showed
     * */
    fun setLastTimeOPAShow(): AdsConfig {
        if (mApplication != null) {
            prefRepository.setLastTimeOPAShowTimeStamp(System.currentTimeMillis())
        }
        return this@AdsConfig
    }

    /*
     * Minimum time to show Interstitial Ads from open app time
     * */
    fun setInterMinimumShowTimeInMs(time: Long): AdsConfig {
        if (mApplication != null) {
            prefRepository.setMinimumTimeShowInterInMs(time)
        }
        return this@AdsConfig
    }

    /*
    * Kiểm tra xem có phải Inter-Ads vừa được load hay không
    * Dùng để fix lỗi load Inter-Ads bị dừng nhạc do bị mất audio focus
    * */
    val isInterstitialJustLoaded: Boolean
        get() = SystemClock.elapsedRealtime() - interstitialLoadedTimestamp < 5000

    /*
     *
     * */
    fun isAdEnable(type: AdsType): Boolean {
        if (mAdsEnableState.containsKey(type.value)) {
            val enable = mAdsEnableState[type.value]
            return enable != null && enable
        }
        return false
    }

    /*
     * This method will check condition time with config FREQ_INTER_OPA_IN_MILLISECONDS
     *
     * return true if current time minus the latest time OPA displayed > FREQ_INTER_OPA_IN_MILLISECONDS has been set
     * */
    fun canShowOPA(): Boolean {
        val freqInterOPAInMilliseconds = prefRepository.lastTimeOPAShowTimeStamp
        if (freqInterOPAInMilliseconds == 0L) {
            return true
        }
        val lastTimeOPAShow = prefRepository.lastTimeOPAShowTimeStamp
        return System.currentTimeMillis() - lastTimeOPAShow >= freqInterOPAInMilliseconds
    }

    /*
    * Splash delay time
    * */
    val splashDelayInMs: Long
        get() = if (mApplication != null) {
            prefRepository.splashDelayInMs
        } else DEFAULT_SPLASH_DELAY_IN_MS

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
            prefRepository.setInterstitialShowedTimestamp(System.currentTimeMillis())
        }
    }

    private val interstitialShowedTimestamp: Long
        get() = if (mApplication != null) {
            prefRepository.interstitialShowedTimestamp
        } else 0L

    fun canShowInterstitial(): Boolean {
        if (mApplication != null) {
            val timePassed = System.currentTimeMillis() - interstitialShowedTimestamp
            return timePassed >= prefRepository.freqCapInterInMs
        }
        return false
    }

    /*
     * AppOpenAd show when resume to foreground
     * */
    fun saveAppOpenAdShowedTimestamp() {
        if (mApplication != null) {
            prefRepository.setAppOpenAdShowedTimeStampKey(System.currentTimeMillis())
        }
    }

    private val appOpenAdShowedTimestamp: Long
        get() = if (mApplication != null) {
            prefRepository.appOpenAdShowedTimeStamp
        } else 0L

    fun canShowAppOpenAd(): Boolean {
        if (mApplication != null) {
            val timePassed = System.currentTimeMillis() - appOpenAdShowedTimestamp
            return timePassed >= freqAppOpenAdInMs
        }
        return false
    }

}
