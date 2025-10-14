package com.naze.parkingfee

import android.app.Application
import com.google.firebase.FirebaseApp
import com.naze.parkingfee.infrastructure.notification.ParkingNotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ParkingFeeApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Firebase 초기화
        FirebaseApp.initializeApp(this)
        
        // 알림 채널 초기화
        ParkingNotificationManager.createNotificationChannel(this)
    }
}
