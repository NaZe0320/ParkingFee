package com.naze.parkingfee.infrastructure.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.naze.parkingfee.MainActivity
import com.naze.parkingfee.R
import com.naze.parkingfee.infrastructure.service.ParkingService
import com.naze.parkingfee.utils.TimeUtils

/**
 * 주차 진행 알림 관리자
 */
object ParkingNotificationManager {
    
    const val CHANNEL_ID = "parking_progress"
    const val NOTIFICATION_ID = 1001
    
    private const val ACTION_STOP_PARKING = "com.naze.parkingfee.STOP_PARKING"
    
    /**
     * 알림 채널을 생성합니다.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "주차 진행 알림",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "주차 진행 상황을 알려주는 알림입니다."
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 주차 진행 알림을 생성합니다.
     */
    fun createParkingNotification(
        context: Context,
        zoneName: String,
        startTime: Long,
        currentFee: Double
    ): NotificationCompat.Builder {
        
        // 앱 열기 인텐트
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val appPendingIntent = PendingIntent.getActivity(
            context, 0, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 주차 종료 인텐트
        val stopIntent = Intent(context, ParkingService::class.java).apply {
            action = ACTION_STOP_PARKING
        }
        val stopPendingIntent = PendingIntent.getService(
            context, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val elapsedTime = TimeUtils.formatDuration(System.currentTimeMillis() - startTime)
        
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_parking)
            .setContentTitle("주차 중 • $zoneName")
            .setContentText("경과 $elapsedTime • ${String.format("%.0f", currentFee)}원")
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setContentIntent(appPendingIntent)
            .addAction(
                R.drawable.ic_stop,
                "종료",
                stopPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
    }
    
    /**
     * 알림을 업데이트합니다.
     */
    fun updateNotification(
        context: Context,
        zoneName: String,
        startTime: Long,
        currentFee: Double
    ) {
        val notification = createParkingNotification(context, zoneName, startTime, currentFee).build()
        
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    /**
     * 알림을 제거합니다.
     */
    fun cancelNotification(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(NOTIFICATION_ID)
    }
}