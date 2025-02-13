package com.mmt.app.data.repository.dataStore

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.mmt.BuildConfig
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.ADS_ENABLE_STATE
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.APP_OPEN_AD_SHOWED_TIMESTAMP
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.DEFAULT_FREQ_CAP_APP_OPEN_AD_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.DEFAULT_FREQ_CAP_INTER_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.DEFAULT_FREQ_CAP_INTER_OPA_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.DEFAULT_INTER_OPA_PROGRESS_DELAY_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.DEFAULT_INTER_OPA_SPLASH_DELAY_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.DEFAULT_WAITING_TIME_WHEN_LOAD_FAILED_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.FREQ_CAP_APP_OPEN_AD_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.FREQ_CAP_INTER_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.FREQ_INTER_OPA_IN_MILLISECONDS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.INTERSTITIAL_OPA_SHOWED_TIMESTAMP
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.INTERSTITIAL_SHOWED_TIMESTAMP
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.INTER_OPA_PROGRESS_DELAY_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.INTER_OPA_SPLASH_DELAY_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.MINIMUM_TIME_SHOW_INTER_IN_MS
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.PREF_CONSENT_ACCEPTED
import com.mmt.ads.data.repository.dataStore.PrefRepository.PreferenceKeys.WAITING_TIME_WHEN_LOAD_FAILED
import com.mmt.common.data.local.BaseDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking

class PrefRepository(context: Context) : BaseDataStore() {

    companion object {
        private var _instance: PrefRepository? = null
        fun getInstance(context: Context): PrefRepository {
            return _instance ?: synchronized(this) {
                _instance ?: PrefRepository(context).also { _instance = it }
            }
        }
    }

    object PreferenceKeys {
        const val AUTO_RESTART = "AUTO_RESTART"
    }

    init {
        dataStore = PreferenceDataStoreFactory.create(corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() }),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { context.preferencesDataStoreFile(BuildConfig.APPLICATION_ID) })
    }

    private val appOpenAdShowedTimeStampKey = longPreferencesKey(APP_OPEN_AD_SHOWED_TIMESTAMP)

}