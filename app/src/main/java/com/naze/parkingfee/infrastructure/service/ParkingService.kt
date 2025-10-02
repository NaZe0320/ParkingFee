package com.naze.parkingfee.infrastructure.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.naze.parkingfee.domain.usecase.GetActiveParkingSessionUseCase
import com.naze.parkingfee.domain.usecase.GetParkingZonesUseCase
import com.naze.parkingfee.domain.usecase.StopParkingUseCase
import com.naze.parkingfee.infrastructure.notification.ParkingNotificationManager
import com.naze.parkingfee.utils.FeeCalculator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 주차 관련 백그라운드 서비스
 */
@AndroidEntryPoint
class ParkingService : LifecycleService() {

    @Inject
    lateinit var getActiveParkingSessionUseCase: GetActiveParkingSessionUseCase
    
    @Inject
    lateinit var getParkingZonesUseCase: GetParkingZonesUseCase
    
    @Inject
    lateinit var stopParkingUseCase: StopParkingUseCase

    companion object {
        const val ACTION_START_PARKING = "com.naze.parkingfee.START_PARKING"
        const val ACTION_STOP_PARKING = "com.naze.parkingfee.STOP_PARKING"
    }

    private var monitoringJob: kotlinx.coroutines.Job? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        when (intent?.action) {
            ACTION_START_PARKING -> startParkingMonitoring()
            ACTION_STOP_PARKING -> stopParkingAndService()
        }
        
        return START_STICKY
    }

    private fun startParkingMonitoring() {
        lifecycleScope.launch {
            try {
                val activeSession = getActiveParkingSessionUseCase.execute()
                if (activeSession != null) {
                    val zones = getParkingZonesUseCase.execute()
                    val zone = zones.firstOrNull { it.id == activeSession.zoneId }
                    
                    if (zone != null) {
                        // 포그라운드 서비스 시작
                        val notification = ParkingNotificationManager.createParkingNotification(
                            this@ParkingService,
                            zone.name,
                            activeSession.startTime,
                            FeeCalculator.calculateFeeForZone(
                                activeSession.startTime,
                                System.currentTimeMillis(),
                                zone
                            )
                        ).build()
                        
                        startForeground(ParkingNotificationManager.NOTIFICATION_ID, notification)
                        
                        // 주기적 업데이트 시작
                        startPeriodicUpdate(activeSession, zone)
                    }
                }
            } catch (e: Exception) {
                // 에러 처리
                stopSelf()
            }
        }
    }
    
    private fun startPeriodicUpdate(session: com.naze.parkingfee.domain.model.ParkingSession, zone: com.naze.parkingfee.domain.model.ParkingZone) {
        monitoringJob = lifecycleScope.launch {
            while (true) {
                try {
                    val currentTime = System.currentTimeMillis()
                    val currentFee = FeeCalculator.calculateFeeForZone(session.startTime, currentTime, zone)
                    
                    ParkingNotificationManager.updateNotification(
                        this@ParkingService,
                        zone.name,
                        session.startTime,
                        currentFee
                    )
                    
                    delay(60000) // 1분마다 업데이트
                } catch (e: Exception) {
                    // 에러 처리
                    break
                }
            }
        }
    }

    private fun stopParkingAndService() {
        lifecycleScope.launch {
            try {
                val activeSession = getActiveParkingSessionUseCase.execute()
                if (activeSession != null) {
                    stopParkingUseCase.execute(activeSession.id)
                }
            } catch (e: Exception) {
                // 에러 처리
            } finally {
                // 알림 제거 및 서비스 종료
                ParkingNotificationManager.cancelNotification(this@ParkingService)
                monitoringJob?.cancel()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        monitoringJob?.cancel()
    }
}
