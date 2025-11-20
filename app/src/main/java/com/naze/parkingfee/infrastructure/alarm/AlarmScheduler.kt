package com.naze.parkingfee.infrastructure.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import javax.inject.Inject

/**
 * 알람 스케줄링을 담당하는 클래스
 */
class AlarmScheduler @Inject constructor(
    private val context: Context
) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    /**
     * 알람을 예약합니다.
     * @param alarmId 알람 ID
     * @param scheduledTime 알람이 발생할 시간 (밀리초)
     * @param targetAmount 목표 금액
     * @param minutesBefore n분 전
     */
    fun scheduleAlarm(
        alarmId: String,
        scheduledTime: Long,
        targetAmount: Double,
        minutesBefore: Int
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_PARKING_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmReceiver.EXTRA_TARGET_AMOUNT, targetAmount)
            putExtra(AlarmReceiver.EXTRA_MINUTES_BEFORE, minutesBefore)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Android 12+ (API 31+)에서는 정확한 알람 권한이 필요합니다
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    scheduledTime,
                    pendingIntent
                )
            } else {
                // 정확한 알람을 예약할 수 없으면 일반 알람 사용
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    scheduledTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                scheduledTime,
                pendingIntent
            )
        }
    }
    
    /**
     * 알람을 취소합니다.
     * @param alarmId 알람 ID
     */
    fun cancelAlarm(alarmId: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_PARKING_ALARM
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}

