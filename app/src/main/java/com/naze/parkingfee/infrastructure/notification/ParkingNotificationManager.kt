package com.naze.parkingfee.infrastructure.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
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
    
    /**
     * 알림 채널을 생성합니다.
     */
    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "주차 진행 알림",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "주차 진행 상황을 알려주는 알림입니다."
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setBypassDnd(true) // 방해 금지 모드 우회
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    
    /**
     * 주차 진행 알림을 생성합니다.
     */
    fun createParkingNotification(
        context: Context,
        zoneName: String,
        startTime: Long,
        currentFee: Double,
        mediaSession: android.support.v4.media.session.MediaSessionCompat? = null
    ): NotificationCompat.Builder {
        
        // 앱 열기 인텐트
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val appPendingIntent = PendingIntent.getActivity(
            context, 0, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 알림 삭제 인텐트
        val deleteIntent = Intent(context, ParkingService::class.java).apply {
            action = ParkingService.ACTION_NOTIFICATION_DISMISSED
        }
        val deletePendingIntent = PendingIntent.getService(
            context, 1, deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val elapsedTime = TimeUtils.formatDuration(System.currentTimeMillis() - startTime)
        val formattedFee = String.format("%.0f", currentFee)
        
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_parking)
            .setContentTitle("🚗 주차 중 • $zoneName")
            .setContentText("⏰ 경과: $elapsedTime  💰 요금: ${formattedFee}원")
            .setSubText("주차 진행 중")
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setContentIntent(appPendingIntent)
            .setDeleteIntent(deletePendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🚗 주차 중 • $zoneName\n\n⏰ 경과: $elapsedTime\n💰 요금: ${formattedFee}원\n\n주차 진행 중입니다.")
            )
            .setStyle(
                MediaNotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setFullScreenIntent(appPendingIntent, false)
    }
    
    /**
     * 알림을 업데이트합니다.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun updateNotification(
        context: Context,
        zoneName: String,
        startTime: Long,
        currentFee: Double,
        mediaSession: android.support.v4.media.session.MediaSessionCompat? = null
    ) {
        val notification = createParkingNotification(context, zoneName, startTime, currentFee, mediaSession).build()
        
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