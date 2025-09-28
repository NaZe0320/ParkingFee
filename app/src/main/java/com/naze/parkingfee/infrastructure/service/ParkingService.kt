package com.naze.parkingfee.infrastructure.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.naze.parkingfee.domain.usecase.GetActiveParkingSessionUseCase
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

    override fun onCreate() {
        super.onCreate()
        startParkingMonitoring()
    }

    private fun startParkingMonitoring() {
        lifecycleScope.launch {
            while (true) {
                try {
                    val activeSession = getActiveParkingSessionUseCase.execute()
                    if (activeSession != null) {
                        // 주차 세션 모니터링 로직
                        updateParkingFee(activeSession)
                    }
                } catch (e: Exception) {
                    // 에러 처리
                }
                
                delay(60000) // 1분마다 체크
            }
        }
    }

    private suspend fun updateParkingFee(session: com.naze.parkingfee.domain.model.ParkingSession) {
        // 주차 요금 업데이트 로직
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
}
