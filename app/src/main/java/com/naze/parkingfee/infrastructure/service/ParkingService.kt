package com.naze.parkingfee.infrastructure.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.naze.parkingfee.domain.usecase.GetActiveParkingSessionUseCase
import com.naze.parkingfee.domain.usecase.GetParkingZonesUseCase
import com.naze.parkingfee.domain.usecase.StopParkingUseCase
import com.naze.parkingfee.infrastructure.notification.ParkingNotificationManager
import com.naze.parkingfee.utils.FeeCalculator
import com.naze.parkingfee.utils.TimeUtils
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
    private var mediaSession: MediaSessionCompat? = null

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
                        // MediaSession 생성 (재생바 숨기기 위해 최소한의 설정)
                        mediaSession = MediaSessionCompat(this@ParkingService, "ParkingService")
                        mediaSession?.isActive = true
                        
                        // 재생바를 숨기기 위한 메타데이터 설정
                        val metadata = android.support.v4.media.MediaMetadataCompat.Builder()
                            .putString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE, "주차 진행 중 • ${zone.name}")
                            .putString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST, "⏰ 경과: ${TimeUtils.formatDuration(System.currentTimeMillis() - activeSession.startTime)}  💰 요금: ${String.format("%.0f", FeeCalculator.calculateFeeForZone(activeSession.startTime, System.currentTimeMillis(), zone))}원")
                            .putLong(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION, -1) // 재생바 숨기기
                            .build()
                        mediaSession?.setMetadata(metadata)
                        
                        // 포그라운드 서비스 시작
                        val notification = ParkingNotificationManager.createParkingNotification(
                            this@ParkingService,
                            zone.name,
                            activeSession.startTime,
                            FeeCalculator.calculateFeeForZone(
                                activeSession.startTime,
                                System.currentTimeMillis(),
                                zone
                            ),
                            mediaSession
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
                    
                    // MediaSession 메타데이터 업데이트
                    val metadata = android.support.v4.media.MediaMetadataCompat.Builder()
                        .putString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE, "주차 진행 중 • ${zone.name}")
                        .putString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST, "⏰ 경과: ${TimeUtils.formatDuration(currentTime - session.startTime)}  💰 요금: ${String.format("%.0f", currentFee)}원")
                        .putLong(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION, -1) // 재생바 숨기기
                        .build()
                    mediaSession?.setMetadata(metadata)
                    
                    ParkingNotificationManager.updateNotification(
                        this@ParkingService,
                        zone.name,
                        session.startTime,
                        currentFee,
                        mediaSession
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
                mediaSession?.isActive = false
                mediaSession?.release()
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
        mediaSession?.isActive = false
        mediaSession?.release()
    }
}
