package com.naze.parkingfee.domain.usecase.parkingsession

import com.naze.parkingfee.domain.model.ParkingSession
import com.naze.parkingfee.domain.repository.ParkingRepository
import javax.inject.Inject

/**
 * 주차 세션의 무료 시간을 업데이트하는 UseCase
 */
class UpdateFreeTimeUseCase @Inject constructor(
    private val parkingRepository: ParkingRepository
) {
    suspend fun execute(sessionId: String, freeTimeMinutes: Int): ParkingSession {
        val session = parkingRepository.getActiveParkingSession()
            ?: throw IllegalStateException("활성 주차 세션이 없습니다.")
        
        if (session.id != sessionId) {
            throw IllegalArgumentException("주차 세션 ID가 일치하지 않습니다.")
        }
        
        val updatedSession = session.copy(freeTimeMinutes = freeTimeMinutes)
        return parkingRepository.updateParkingSession(updatedSession)
    }
}

