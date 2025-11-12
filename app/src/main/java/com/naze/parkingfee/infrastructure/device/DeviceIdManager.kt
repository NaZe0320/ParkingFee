package com.naze.parkingfee.infrastructure.device

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 디바이스 ID 관리자
 * Android_ID를 가져오는 기능을 제공합니다.
 */
@Singleton
class DeviceIdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Android_ID를 가져옵니다.
     * 
     * @return Android_ID 문자열. 가져올 수 없는 경우 null을 반환합니다.
     */
    fun getAndroidId(): String? {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                .takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }
}

