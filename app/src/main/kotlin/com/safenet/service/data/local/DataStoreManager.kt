package com.safenet.service.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.safenet.service.data.local.DataStoreManager.PreferenceKeys.ACCESS_TOKEN
import com.safenet.service.data.local.DataStoreManager.PreferenceKeys.CODE
import com.safenet.service.data.local.DataStoreManager.PreferenceKeys.IS_CONNECTED
import com.safenet.service.data.local.DataStoreManager.PreferenceKeys.PUBLIC_S
import com.safenet.service.data.local.DataStoreManager.PreferenceKeys.SERVER_AVAILABILITY
import com.safenet.service.data.local.DataStoreManager.PreferenceKeys.SERVER_ID
import com.safenet.service.data.local.DataStoreManager.PreferenceKeys.UPP_LLIINK
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "safenet_dataStore"
)

class DataStoreManager @Inject constructor(@ApplicationContext val context: Context) {

    fun <T> getData(key: Preferences.Key<T>): Flow<T?> = context.dataStore.data.map { preferences ->
        preferences[key]
    }

    suspend fun <T> updateData(key: Preferences.Key<T>, value: T) =
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }

    suspend fun clearDataStore() =
        context.dataStore.edit {
            it.clear()
        }

    suspend fun clearData() {
        val keys = arrayListOf(ACCESS_TOKEN, PUBLIC_S, IS_CONNECTED, UPP_LLIINK, CODE, SERVER_AVAILABILITY, SERVER_ID)
        for (key in keys) {
            context.dataStore.edit {
                it.remove(key)
            }
        }
    }


    object PreferenceKeys {
        val IS_UPDATE_MODE = booleanPreferencesKey(name = "is_update_mode")
        val ACCESS_TOKEN = stringPreferencesKey(name = "access_token")
        val PUBLIC_S = stringPreferencesKey(name = "public_s")
        val IS_CONNECTED = booleanPreferencesKey(name = "is_connected")
        val BASE_URL = stringPreferencesKey(name = "base_url_counter")
        val UPP_LLIINK = stringPreferencesKey(name = "upp_lliink")
        val CODE = stringPreferencesKey(name = "CODE")
        val SERVER_AVAILABILITY = stringPreferencesKey(name = "serverAvailability")
        val SERVER_ID = intPreferencesKey(name = "server_id")
    }

}