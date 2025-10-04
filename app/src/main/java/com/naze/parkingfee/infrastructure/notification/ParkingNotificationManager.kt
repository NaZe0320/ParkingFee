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
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "주차 진행 상황을 알려주는 알림입니다."
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    
    /**
     * 주차 진행 알림을 생성합니다. (Ongoing Foreground Notification)
     */
    fun createParkingNotification(
        context: Context,
        zoneName: String,
        startTime: Long,
        currentFee: Double,
        hasDiscount: Boolean = false,
        originalFee: Double? = null,
        stopIntent: PendingIntent
    ): Notification {
        // 앱 열기 인텐트
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val appPendingIntent = PendingIntent.getActivity(
            context, 0, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val elapsedTime = TimeUtils.formatDuration(System.currentTimeMillis() - startTime)
        val formattedFee = String.format("%.0f", currentFee)
        
        val feeText = if (hasDiscount && originalFee != null) {
            val formattedOriginalFee = String.format("%.0f", originalFee)
            val discountPercent = ((1 - currentFee / originalFee) * 100).toInt()
            "💰 요금: ~~${formattedOriginalFee}원~~ → ${formattedFee}원 (${discountPercent}% 할인)"
        } else {
            "💰 요금: ${formattedFee}원"
        }
        
        val bigText = if (hasDiscount && originalFee != null) {
            // val formattedOriginalFee = String.format("%.0f", originalFee)
            val discountPercent = ((1 - currentFee / originalFee) * 100).toInt()
            "🚗 주차 중 • $zoneName\n\n⏰ 경과: $elapsedTime\n💰 요금: ${formattedFee}원 (${discountPercent}% 할인 적용용)\n\n주차 진행 중입니다."
        } else {
            "🚗 주차 중 • $zoneName\n\n⏰ 경과: $elapsedTime\n💰 요금: ${formattedFee}원\n\n주차 진행 중입니다."
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // ic_stat_parking 리소스가 없으므로 임시 아이콘으로 대체
            .setContentTitle("🚗 주차 중 • $zoneName")
            .setContentText("⏰ 경과: $elapsedTime  $feeText")
            .setSubText("주차 진행 중")
            .setWhen(startTime)
            .setUsesChronometer(false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(appPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "정지", stopIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bigText)
            )
            .build()
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
        hasDiscount: Boolean = false,
        originalFee: Double? = null
    ) {
        // 정지 액션 인텐트 생성
        val stopIntent = Intent(context, ParkingService::class.java).apply {
            action = ParkingService.ACTION_STOP_PARKING
        }
        val stopPendingIntent = PendingIntent.getService(
            context, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = createParkingNotification(
            context = context,
            zoneName = zoneName,
            startTime = startTime,
            currentFee = currentFee,
            hasDiscount = hasDiscount,
            originalFee = originalFee,
            stopIntent = stopPendingIntent
        )

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