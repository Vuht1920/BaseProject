package com.mmt.ads

import androidx.datastore.preferences.core.stringPreferencesKey

interface PreferenceRepository {
    object PreferenceKeys {
        private const val APP_OPEN_AD_SHOWED_TIMESTAMP = "app_open_ad_showed_timestamp"
        private const val INTERSTITIAL_OPA_SHOWED_TIMESTAMP = "interstitial_opa_showed_timestamp"
        private const val INTERSTITIAL_SHOWED_TIMESTAMP = "interstitial_showed_timestamp"
        private const val FREQ_INTER_OPA_IN_MILLISECONDS = "freq_interstitial_opa_in_ms"
        private const val INTER_OPA_SPLASH_DELAY_IN_MS = "inter_opa_splash_delay_in_ms"
        private const val INTER_OPA_PROGRESS_DELAY_IN_MS = "inter_opa_progress_delay_in_ms"
        private const val FREQ_CAP_INTER_IN_MS = "freq_cap_inter_in_ms"
        private const val ADS_ENABLE_STATE = "ads_enable_state"
        private const val WAITING_TIME_WHEN_LOAD_FAILED = "waiting_time_when_load_failed"
        private const val FREQ_CAP_APP_OPEN_AD_IN_MS = "freq_cap_app_open_ad_in_ms"

        const val DEFAULT_FREQ_CAP_APP_OPEN_AD_IN_MS = (5 * 60 * 1000).toLong()// 5 minutes
        const val DEFAULT_FREQ_CAP_INTER_OPA_IN_MS = (15 * 60 * 1000).toLong()// 15 minutes
        const val DEFAULT_INTER_OPA_SPLASH_DELAY_IN_MS: Long = 3000 // 3 seconds
        const val DEFAULT_INTER_OPA_PROGRESS_DELAY_IN_MS: Long = 2000 // 2 seconds
        const val DEFAULT_FREQ_CAP_INTER_IN_MS = (15 * 60 * 1000).toLong() // 15 minutes
        const val DEFAULT_WAITING_TIME_WHEN_LOAD_FAILED_IN_MS = (5 * 1000).toLong()  // 5s


        val appOpenAdShowedTimeStamp = stringPreferencesKey(APP_OPEN_AD_SHOWED_TIMESTAMP)
        val lastTimeOPAShowTimeStamp = stringPreferencesKey(INTERSTITIAL_OPA_SHOWED_TIMESTAMP)
        val interstitialShowedTimestamp = stringPreferencesKey(INTERSTITIAL_SHOWED_TIMESTAMP)
        val freqInterOPAInMilliseconds = stringPreferencesKey(FREQ_INTER_OPA_IN_MILLISECONDS)
        val interOPASplashDelayInMs = stringPreferencesKey(INTER_OPA_SPLASH_DELAY_IN_MS)
        val interOPAProgressDelayInMs = stringPreferencesKey(INTER_OPA_PROGRESS_DELAY_IN_MS)
        val freqCapInterInMs = stringPreferencesKey(FREQ_CAP_INTER_IN_MS)
        val adsEnableState = stringPreferencesKey(ADS_ENABLE_STATE)
        val waitingTimeWhenLoadFailedInMs = stringPreferencesKey(WAITING_TIME_WHEN_LOAD_FAILED)
        val freqCapAppOpenAdInMs = stringPreferencesKey(FREQ_CAP_APP_OPEN_AD_IN_MS)
    }
}