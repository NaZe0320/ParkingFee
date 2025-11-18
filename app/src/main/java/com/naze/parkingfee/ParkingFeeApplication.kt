package com.naze.parkingfee

import android.app.Application
import com.naze.parkingfee.infrastructure.notification.ParkingNotificationManager
import com.naze.parkingfee.infrastructure.notification.AlarmNotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ParkingFeeApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 알림 채널 초기화
        ParkingNotificationManager.createNotificationChannel(this)
        AlarmNotificationManager.createNotificationChannel(this)
    }
}
