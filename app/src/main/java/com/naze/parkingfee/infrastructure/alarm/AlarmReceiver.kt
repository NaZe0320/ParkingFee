package com.naze.parkingfee.infrastructure.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.naze.parkingfee.infrastructure.notification.AlarmNotificationManager

/**
 * 알람을 수신하는 BroadcastReceiver
 * 
 * Note: BroadcastReceiver는 @AndroidEntryPoint를 지원하지 않으므로
 * 의존성 주입 없이 알림만 표시합니다.
 * 알람 triggered 상태 업데이트는 앱 실행 중 자동으로 처리됩니다.
 */
class AlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_PARKING_ALARM) {
            val alarmId = intent.getStringExtra(EXTRA_ALARM_ID) ?: return
            val targetAmount = intent.getDoubleExtra(EXTRA_TARGET_AMOUNT, 0.0)
            val minutesBefore = intent.getIntExtra(EXTRA_MINUTES_BEFORE, 0)
            
            // 알림 표시
            AlarmNotificationManager.showAlarmNotification(
                context,
                alarmId,
                targetAmount,
                minutesBefore
            )
        }
    }
    
    companion object {
        const val ACTION_PARKING_ALARM = "com.naze.parkingfee.ACTION_PARKING_ALARM"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_TARGET_AMOUNT = "extra_target_amount"
        const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"
    }
}

