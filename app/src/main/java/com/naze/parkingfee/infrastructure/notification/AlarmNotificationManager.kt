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

/**
 * 알람 알림 관리자
 */
object AlarmNotificationManager {
    
    private const val CHANNEL_ID = "parking_alarm_channel"
    private const val CHANNEL_NAME = "주차 알람"
    private const val CHANNEL_DESCRIPTION = "주차 요금 목표 금액 도달 알림"
    const val NOTIFICATION_ID_BASE = 2000
    
    /**
     * 알림 채널을 생성합니다.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 알람 알림을 표시합니다.
     */
    fun showAlarmNotification(
        context: Context,
        alarmId: String,
        targetAmount: Double,
        minutesBefore: Int
    ) {
        createNotificationChannel(context)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("주차 요금 알림")
            .setContentText("${minutesBefore}분 후 주차 요금이 ${String.format("%.0f", targetAmount)}원에 도달합니다")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(NOTIFICATION_ID_BASE + alarmId.hashCode(), notification)
    }
    
    /**
     * 알람 알림을 취소합니다.
     */
    fun cancelAlarmNotification(context: Context, alarmId: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(NOTIFICATION_ID_BASE + alarmId.hashCode())
    }
}

