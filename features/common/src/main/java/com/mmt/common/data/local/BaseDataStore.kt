package com.mmt.common.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException

abstract class BaseDataStore {
    open var dataStore: DataStore<Preferences>? = null
    open fun <T> getPreference(key: Preferences.Key<T>, default: T): Flow<T?> {
        dataStore?.let { it ->
            return it.data.catch { exception ->
                /*
                 * dataStore.data throws an IOException when an error
                 * is encountered when reading data
                 */
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                preferences[key] ?: default
            }
        } ?: run {
            return flowOf(default)
        }
    }


    fun <T> getPreferenceBlocking(key: Preferences.Key<T>, default: T): T = runBlocking {
        return@runBlocking try {
            dataStore?.data?.first()?.get(key) ?: default
        } catch (e: Exception) {
            default
        }
    }

    open suspend fun <T> setPreference(key: Preferences.Key<T>, value: T) {
        dataStore?.edit { preferences ->
            preferences[key] = value
        }
    }
}