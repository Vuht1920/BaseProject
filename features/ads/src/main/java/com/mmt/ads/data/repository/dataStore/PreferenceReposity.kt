package com.mmt.ads.data.repository.dataStore

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.ADS_ENABLE_STATE
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.APP_OPEN_AD_SHOWED_TIMESTAMP
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.DEFAULT_FREQ_CAP_APP_OPEN_AD_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.DEFAULT_FREQ_CAP_INTER_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.DEFAULT_FREQ_CAP_INTER_OPA_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.DEFAULT_INTER_OPA_PROGRESS_DELAY_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.DEFAULT_SPLASH_DELAY_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.DEFAULT_WAITING_TIME_WHEN_LOAD_FAILED_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.FREQ_CAP_APP_OPEN_AD_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.FREQ_CAP_INTER_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.FREQ_INTER_OPA_IN_MILLISECONDS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.INTERSTITIAL_OPA_SHOWED_TIMESTAMP
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.INTERSTITIAL_SHOWED_TIMESTAMP
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.INTER_OPA_PROGRESS_DELAY_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.MINIMUM_TIME_SHOW_INTER_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.PREF_CONSENT_ACCEPTED
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.SPLASH_DELAY_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.WAITING_TIME_WHEN_LOAD_FAILED
import com.mmt.common.data.local.BaseDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking

class PrefRepository(context: Context) : BaseDataStore() {

    companion object {
        private const val PREFERENCES = "mmt_ads"

        private var _instance: PrefRepository? = null
        fun getInstance(context: Context): PrefRepository {
            return _instance ?: synchronized(this) {
                _instance ?: PrefRepository(context).also { _instance = it }
            }
        }
    }

    object PreferenceKeys {
        const val APP_OPEN_AD_SHOWED_TIMESTAMP = "app_open_ad_showed_timestamp"
        const val INTERSTITIAL_OPA_SHOWED_TIMESTAMP = "interstitial_opa_showed_timestamp"
        const val INTERSTITIAL_SHOWED_TIMESTAMP = "interstitial_showed_timestamp"
        const val FREQ_INTER_OPA_IN_MILLISECONDS = "freq_interstitial_opa_in_ms"
        const val SPLASH_DELAY_IN_MS = "splash_delay_in_ms"
        const val INTER_OPA_PROGRESS_DELAY_IN_MS = "inter_opa_progress_delay_in_ms"
        const val FREQ_CAP_INTER_IN_MS = "freq_cap_inter_in_ms"
        const val ADS_ENABLE_STATE = "ads_enable_state"
        const val WAITING_TIME_WHEN_LOAD_FAILED = "waiting_time_when_load_failed"
        const val FREQ_CAP_APP_OPEN_AD_IN_MS = "freq_cap_app_open_ad_in_ms"
        const val MINIMUM_TIME_SHOW_INTER_IN_MS = "minimum_time_show_inter_in_ms"
        const val PREF_CONSENT_ACCEPTED = "pref_consent_accepted"

        const val DEFAULT_FREQ_CAP_APP_OPEN_AD_IN_MS = (5 * 60 * 1000).toLong()// 5 minutes
        const val DEFAULT_FREQ_CAP_INTER_OPA_IN_MS = (15 * 60 * 1000).toLong()// 15 minutes
        const val DEFAULT_SPLASH_DELAY_IN_MS: Long = 3000 // 3 seconds
        const val DEFAULT_INTER_OPA_PROGRESS_DELAY_IN_MS: Long = 2000 // 2 seconds
        const val DEFAULT_FREQ_CAP_INTER_IN_MS = (15 * 60 * 1000).toLong() // 15 minutes
        const val DEFAULT_WAITING_TIME_WHEN_LOAD_FAILED_IN_MS = (5 * 1000).toLong()  // 5s
    }

    init {
        dataStore = PreferenceDataStoreFactory.create(corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() }),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { context.preferencesDataStoreFile(PREFERENCES) })
    }

    private val appOpenAdShowedTimeStampKey = longPreferencesKey(APP_OPEN_AD_SHOWED_TIMESTAMP)
    private val lastTimeOPAShowTimeStampKey = longPreferencesKey(INTERSTITIAL_OPA_SHOWED_TIMESTAMP)
    private val interstitialShowedTimestampKey = longPreferencesKey(INTERSTITIAL_SHOWED_TIMESTAMP)
    private val freqInterOPAInMillisecondsKey = longPreferencesKey(FREQ_INTER_OPA_IN_MILLISECONDS)
    private val splashDelayInMsKey = longPreferencesKey(SPLASH_DELAY_IN_MS)
    private val interOPAProgressDelayInMsKey = longPreferencesKey(INTER_OPA_PROGRESS_DELAY_IN_MS)
    private val freqCapInterInMsKey = longPreferencesKey(FREQ_CAP_INTER_IN_MS)
    private val adsEnableStateKey = stringPreferencesKey(ADS_ENABLE_STATE)
    private val waitingTimeWhenLoadFailedInMsKey = longPreferencesKey(WAITING_TIME_WHEN_LOAD_FAILED)
    private val freqCapAppOpenAdInMsKey = longPreferencesKey(FREQ_CAP_APP_OPEN_AD_IN_MS)
    private val minimumTimeShowInterInMsKey = longPreferencesKey(MINIMUM_TIME_SHOW_INTER_IN_MS)
    private val consentAcceptedKey = booleanPreferencesKey(PREF_CONSENT_ACCEPTED)

    fun setAppOpenAdShowedTimeStampKey(value: Long) = runBlocking { setPreference(appOpenAdShowedTimeStampKey, value) }
    val appOpenAdShowedTimeStamp get() = getPreferenceBlocking(appOpenAdShowedTimeStampKey, 0L)

    fun setFreqInterOPAInMilliseconds(value: Long) = runBlocking { setPreference(freqInterOPAInMillisecondsKey, value) }
    val freqInterOPAInMilliseconds get() = getPreferenceBlocking(freqInterOPAInMillisecondsKey, 0L)

    fun setLastTimeOPAShowTimeStamp(value: Long) = runBlocking { setPreference(lastTimeOPAShowTimeStampKey, value) }
    val lastTimeOPAShowTimeStamp get() = getPreferenceBlocking(lastTimeOPAShowTimeStampKey, DEFAULT_FREQ_CAP_INTER_OPA_IN_MS)

    fun setInterstitialShowedTimestamp(value: Long) = runBlocking { setPreference(interstitialShowedTimestampKey, value) }
    val interstitialShowedTimestamp get() = getPreferenceBlocking(interstitialShowedTimestampKey, 0L)

    fun setSplashDelayInMs(value: Long) = runBlocking { setPreference(splashDelayInMsKey, value) }
    val splashDelayInMs get() = getPreferenceBlocking(splashDelayInMsKey, DEFAULT_SPLASH_DELAY_IN_MS)

    fun setInterOPAProgressDelayInMs(value: Long) = runBlocking { setPreference(interOPAProgressDelayInMsKey, value) }
    val interOPAProgressDelayInMs get() = getPreferenceBlocking(interOPAProgressDelayInMsKey, DEFAULT_INTER_OPA_PROGRESS_DELAY_IN_MS)

    fun setFreqCapInterInMs(value: Long) = runBlocking { setPreference(freqCapInterInMsKey, value) }
    val freqCapInterInMs get() = getPreferenceBlocking(freqCapInterInMsKey, DEFAULT_FREQ_CAP_INTER_IN_MS)

    fun setAdsEnableState(value: String) = runBlocking { setPreference(adsEnableStateKey, value) }
    val adsEnableState get() = getPreferenceBlocking(adsEnableStateKey, "{}")

    fun setWaitingTimeWhenLoadFailedInMs(value: Long) = runBlocking { setPreference(waitingTimeWhenLoadFailedInMsKey, value) }
    val waitingTimeWhenLoadFailedInMs get() = getPreferenceBlocking(waitingTimeWhenLoadFailedInMsKey, DEFAULT_WAITING_TIME_WHEN_LOAD_FAILED_IN_MS)

    fun setFreqCapAppOpenAdInMs(value: Long) = runBlocking { setPreference(freqCapAppOpenAdInMsKey, value) }
    val freqCapAppOpenAdInMs get() = getPreferenceBlocking(freqCapAppOpenAdInMsKey, DEFAULT_FREQ_CAP_APP_OPEN_AD_IN_MS)

    fun setConsentAccepted(value: Boolean) = runBlocking { setPreference(consentAcceptedKey, value) }
    val isConsentAccepted get() = getPreferenceBlocking(consentAcceptedKey, false)

    fun setMinimumTimeShowInterInMs(value: Long) = runBlocking { setPreference(minimumTimeShowInterInMsKey, value) }
    val minimumTimeShowInterInMs get() = getPreferenceBlocking(minimumTimeShowInterInMsKey, 2000)

}