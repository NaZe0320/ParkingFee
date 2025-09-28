package com.naze.parkingfee.infrastructure.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.naze.parkingfee.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 주차 관련 알림 관리자
 */
@Singleton
class ParkingNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "주차 알림",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "주차 시작/종료 알림"
        }
        
        notificationManager.createNotificationChannel(channel)
    }
    
    fun showParkingStartNotification(zoneName: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("주차 시작")
            .setContentText("$zoneName 구역에서 주차가 시작되었습니다.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_START, notification)
    }
    
    fun showParkingStopNotification(totalFee: Double) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("주차 종료")
            .setContentText("주차가 종료되었습니다. 총 요금: ${String.format("%.0f", totalFee)}원")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_STOP, notification)
    }
    
    companion object {
        private const val CHANNEL_ID = "parking_channel"
        private const val NOTIFICATION_ID_START = 1001
        private const val NOTIFICATION_ID_STOP = 1002
    }
}
