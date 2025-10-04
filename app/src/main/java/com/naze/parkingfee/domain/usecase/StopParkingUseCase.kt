package com.naze.parkingfee.domain.usecase

import com.naze.parkingfee.domain.model.ParkingSession
import com.naze.parkingfee.domain.model.ParkingHistory
import com.naze.parkingfee.domain.repository.ParkingRepository
import com.naze.parkingfee.domain.repository.ParkingHistoryRepository
import com.naze.parkingfee.utils.TimeUtils
import javax.inject.Inject

/**
 * 주차 종료 UseCase
 */
class StopParkingUseCase @Inject constructor(
    private val parkingRepository: ParkingRepository,
    private val parkingHistoryRepository: ParkingHistoryRepository
) {
    suspend operator fun invoke(sessionId: String): ParkingSession {
        val session = parkingRepository.stopParkingSession(sessionId)
        
        // 주차 기록 저장
        saveParkingHistory(session)
        
        return session
    }
    
    suspend fun execute(sessionId: String): ParkingSession = invoke(sessionId)
    
    private suspend fun saveParkingHistory(session: ParkingSession) {
        try {
            // 구역 정보 조회 (스냅샷용)
            val zone = parkingRepository.getParkingZoneById(session.zoneId)
            val zoneNameSnapshot = zone?.name ?: "알 수 없는 구역"
            
            // 주차 시간 계산 (분 단위)
            val durationMinutes = ((session.endTime!! - session.startTime) / (1000 * 60)).toInt()
            
            val history = ParkingHistory(
                id = "history_${session.id}",
                zoneId = session.zoneId,
                zoneNameSnapshot = zoneNameSnapshot,
                startedAt = session.startTime,
                endedAt = session.endTime,
                durationMinutes = durationMinutes,
                feePaid = session.totalFee ?: 0.0
            )
            
            parkingHistoryRepository.saveParkingHistory(history)
        } catch (e: Exception) {
            // 기록 저장 실패는 로그만 남기고 주차 종료는 정상 처리
            println("Failed to save parking history: ${e.message}")
        }
    }
}
