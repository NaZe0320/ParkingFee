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
        const val ACTION_SYNC_NOTIFICATION = "com.naze.parkingfee.SYNC_NOTIFICATION"
        const val ACTION_NOTIFICATION_DISMISSED = "com.naze.parkingfee.NOTIFICATION_DISMISSED"
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
            ACTION_SYNC_NOTIFICATION -> syncNotificationWithActiveSession()
            ACTION_NOTIFICATION_DISMISSED -> handleNotificationDismissed()
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

    /**
     * 활성 세션과 알림을 동기화합니다.
     * 앱 복귀 시 호출되어 실행 중인 주차 세션이 있으면 알림을 복구합니다.
     */
    private fun syncNotificationWithActiveSession() {
        lifecycleScope.launch {
            try {
                val activeSession = getActiveParkingSessionUseCase.execute()
                if (activeSession != null) {
                    val zones = getParkingZonesUseCase.execute()
                    val zone = zones.firstOrNull { it.id == activeSession.zoneId }
                    
                    if (zone != null) {
                        // 이미 모니터링 중이면 중복 실행 방지
                        if (monitoringJob?.isActive == true) {
                            return@launch
                        }
                        
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
                } else {
                    // 활성 세션이 없으면 알림 제거 및 서비스 정리
                    ParkingNotificationManager.cancelNotification(this@ParkingService)
                    mediaSession?.isActive = false
                    mediaSession?.release()
                    monitoringJob?.cancel()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            } catch (e: Exception) {
                // 에러 처리
                stopSelf()
            }
        }
    }

    /**
     * 알림이 사용자에 의해 삭제되었을 때 호출됩니다.
     * 모니터링 작업을 중지하지만 세션은 종료하지 않습니다.
     */
    private fun handleNotificationDismissed() {
        lifecycleScope.launch {
            try {
                // 모니터링 작업 중지
                monitoringJob?.cancel()
                monitoringJob = null
                
                // MediaSession 정리
                mediaSession?.isActive = false
                mediaSession?.release()
                mediaSession = null
                
                // 포그라운드 서비스 중지 (알림은 이미 제거됨)
                stopForeground(STOP_FOREGROUND_REMOVE)
                
                // 서비스 종료
                stopSelf()
            } catch (e: Exception) {
                // 에러 처리
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
