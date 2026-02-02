package com.naze.parkingfee.data.datasource.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 업데이트 체크(권장 업데이트 스킵 버전 등) 저장용 DataStore
 */
@Singleton
class AppUpdateDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_update")
        private val LAST_SKIPPED_OPTIONAL_VERSION_CODE_KEY =
            longPreferencesKey("last_skipped_optional_version_code")
    }

    suspend fun getLastSkippedOptionalVersionCode(): Long {
        val prefs = context.dataStore.data.first()
        return prefs[LAST_SKIPPED_OPTIONAL_VERSION_CODE_KEY] ?: 0L
    }

    suspend fun setLastSkippedOptionalVersionCode(versionCode: Long) {
        context.dataStore.edit { prefs ->
            prefs[LAST_SKIPPED_OPTIONAL_VERSION_CODE_KEY] = versionCode
        }
    }
}

