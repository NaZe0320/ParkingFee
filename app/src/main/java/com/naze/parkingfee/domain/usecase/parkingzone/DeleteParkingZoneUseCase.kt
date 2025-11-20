package com.naze.parkingfee.domain.usecase.parkingzone

import com.naze.parkingfee.domain.repository.ParkingRepository
import javax.inject.Inject

/**
 * 주차 구역 삭제 결과
 */
sealed class DeleteZoneResult {
    object Success : DeleteZoneResult()
    object ZoneInUse : DeleteZoneResult()
    data class Error(val message: String) : DeleteZoneResult()
}

/**
 * 주차 구역 삭제 UseCase
 * 활성 주차 세션에서 사용 중인 구역은 삭제할 수 없습니다.
 */
class DeleteParkingZoneUseCase @Inject constructor(
    private val parkingRepository: ParkingRepository
) {
    suspend fun execute(zoneId: String): DeleteZoneResult {
        return try {
            // 활성 주차 세션 확인
            val activeSession = parkingRepository.getActiveParkingSession()
            
            // 현재 주차 중인 구역인지 확인
            if (activeSession != null && activeSession.zoneId == zoneId) {
                return DeleteZoneResult.ZoneInUse
            }
            
            // 삭제 수행
            val success = parkingRepository.deleteParkingZone(zoneId)
            if (success) {
                DeleteZoneResult.Success
            } else {
                DeleteZoneResult.Error("삭제에 실패했습니다.")
            }
        } catch (e: Exception) {
            DeleteZoneResult.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
        }
    }
}

