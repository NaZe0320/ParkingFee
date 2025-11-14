package com.naze.parkingfee.domain.usecase.parkingsession

import com.naze.parkingfee.domain.model.ParkingSession
import com.naze.parkingfee.domain.model.ParkingHistory
import com.naze.parkingfee.domain.repository.ParkingRepository
import com.naze.parkingfee.domain.repository.ParkingHistoryRepository
import com.naze.parkingfee.domain.repository.SelectedVehicleRepository
import com.naze.parkingfee.domain.repository.VehicleRepository
import com.naze.parkingfee.utils.FeeCalculator
import com.naze.parkingfee.utils.FeeResult
import com.naze.parkingfee.utils.TimeUtils
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 주차 종료 UseCase
 */
class StopParkingUseCase @Inject constructor(
    private val parkingRepository: ParkingRepository,
    private val parkingHistoryRepository: ParkingHistoryRepository,
    private val selectedVehicleRepository: SelectedVehicleRepository,
    private val vehicleRepository: VehicleRepository
) {
    suspend fun execute(sessionId: String): ParkingSession {
        val session = parkingRepository.stopParkingSession(sessionId)
        
        // 주차 기록 저장
        saveParkingHistory(session)
        
        return session
    }
    
    private suspend fun saveParkingHistory(session: ParkingSession) {
        try {
            // 구역 정보 조회 (스냅샷용)
            val zone = parkingRepository.getParkingZoneById(session.zoneId)
            val zoneNameSnapshot = zone?.name ?: "알 수 없는 구역"
            
            // 주차 시간 계산 (분 단위)
            val durationMinutes = ((session.endTime!! - session.startTime) / (1000 * 60)).toInt()
            
            // 선택 차량 스냅샷 조회
            val selectedVehicleId = selectedVehicleRepository.selectedVehicleId
                .first()
            val vehicle = selectedVehicleId?.let { vehicleRepository.getVehicleById(it) }
            
            // 할인 적용된 요금 계산
            val feeResult = if (zone != null) {
                FeeCalculator.calculateFeeForZoneResult(
                    session.startTime,
                    session.endTime!!,
                    zone,
                    vehicle
                )
            } else {
                // 구역 정보가 없으면 기본 요금 계산
                FeeResult(
                    original = session.totalFee,
                    discounted = session.totalFee
                )
            }
            
            val history = ParkingHistory(
                id = "history_${session.id}",
                zoneId = session.zoneId,
                zoneNameSnapshot = zoneNameSnapshot,
                vehicleId = vehicle?.id,
                vehicleNameSnapshot = vehicle?.displayName,
                vehiclePlateSnapshot = vehicle?.displayPlateNumber,
                startedAt = session.startTime,
                endedAt = session.endTime!!,
                durationMinutes = durationMinutes,
                feePaid = feeResult.discounted,
                originalFee = feeResult.original,
                hasDiscount = feeResult.hasDiscount
            )
            
            parkingHistoryRepository.saveParkingHistory(history)
        } catch (e: Exception) {
            // 기록 저장 실패는 로그만 남기고 주차 종료는 정상 처리
            println("Failed to save parking history: ${e.message}")
        }
    }
}

