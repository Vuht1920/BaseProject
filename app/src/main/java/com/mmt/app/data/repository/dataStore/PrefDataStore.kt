package com.mmt.app.data.repository.dataStore

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.mmt.BuildConfig
import com.mmt.common.data.local.BaseDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.lang.reflect.Array.setBoolean
import javax.inject.Inject

class PrefDataStore @Inject constructor(@ApplicationContext context: Context) : BaseDataStore() {

    init {
        dataStore = PreferenceDataStoreFactory.create(corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() }),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { context.preferencesDataStoreFile(BuildConfig.APPLICATION_ID) })
    }

    companion object {
        private const val AUTO_RESTART = "AUTO_RESTART"
        private const val PREF_SHOW_EXIT_DIALOG = "PREF_SHOW_EXIT_DIALOG"
    }

    /**
     * Tự động khởi động lại App sau 3 lần
     */
    private val autoRestartAppKey = intPreferencesKey(AUTO_RESTART)
    val autoRestartApp get() = getPreferenceBlocking(autoRestartAppKey, 0)
    fun setFlagAutoRestartApp(value: Int) {
        setPreferenceBlocking(autoRestartAppKey, value)
    }

    /**
     * Exit dialog
     */
    private val isShowExitDialogKey = booleanPreferencesKey(PREF_SHOW_EXIT_DIALOG)
    val isShowExitDialog get() = getPreferenceBlocking(isShowExitDialogKey, true)
    fun setShowExitDialog(isShow: Boolean) {
        setPreferenceBlocking(isShowExitDialogKey, isShow)
    }
}