package com.naze.parkingfee.data.datasource.remote.remoteconfig

import android.content.Context
import android.content.pm.ApplicationInfo
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.naze.parkingfee.domain.repository.AppUpdateConfig
import kotlinx.coroutines.tasks.await
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateRemoteConfigDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteConfig: FirebaseRemoteConfig
) {
    companion object {
        const val KEY_FORCE_MIN_VERSION_CODE = "force_update_min_version_code"
        const val KEY_OPTIONAL_LATEST_VERSION_CODE = "optional_update_latest_version_code"
        const val KEY_FORCE_MESSAGE = "force_update_message"
        const val KEY_OPTIONAL_MESSAGE = "optional_update_message"
        const val KEY_STORE_URL = "update_store_url"

        private const val DEFAULT_FORCE_MIN_VERSION_CODE = 0L
        private const val DEFAULT_OPTIONAL_LATEST_VERSION_CODE = 0L
        private const val DEFAULT_FORCE_MESSAGE = "업데이트가 필요합니다."
        private const val DEFAULT_OPTIONAL_MESSAGE = "새 버전이 있습니다. 업데이트하시겠어요?"
    }

    suspend fun fetchAndGet(): AppUpdateConfig {
        val isDebuggable =
            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val fetchIntervalSeconds = if (isDebuggable) 0L else 60L * 60L
        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(fetchIntervalSeconds)
            .build()
        remoteConfig.setConfigSettingsAsync(settings)

        remoteConfig.setDefaultsAsync(
            mapOf(
                KEY_FORCE_MIN_VERSION_CODE to DEFAULT_FORCE_MIN_VERSION_CODE,
                KEY_OPTIONAL_LATEST_VERSION_CODE to DEFAULT_OPTIONAL_LATEST_VERSION_CODE,
                KEY_FORCE_MESSAGE to DEFAULT_FORCE_MESSAGE,
                KEY_OPTIONAL_MESSAGE to DEFAULT_OPTIONAL_MESSAGE,
                KEY_STORE_URL to ""
            )
        )

        remoteConfig.fetchAndActivate().await()

        val forceMin = remoteConfig.getLong(KEY_FORCE_MIN_VERSION_CODE)
        val optionalLatest = remoteConfig.getLong(KEY_OPTIONAL_LATEST_VERSION_CODE)

        val forceMessage = remoteConfig.getString(KEY_FORCE_MESSAGE).takeIf { it.isNotBlank() }
            ?: DEFAULT_FORCE_MESSAGE
        val optionalMessage = remoteConfig.getString(KEY_OPTIONAL_MESSAGE).takeIf { it.isNotBlank() }
            ?: DEFAULT_OPTIONAL_MESSAGE

        val storeUrl = remoteConfig.getString(KEY_STORE_URL).trim().takeIf { it.isNotBlank() }

        return AppUpdateConfig(
            forceUpdateMinVersionCode = forceMin,
            optionalUpdateLatestVersionCode = optionalLatest,
            forceUpdateMessage = forceMessage,
            optionalUpdateMessage = optionalMessage,
            storeUrl = storeUrl
        )
    }
}

