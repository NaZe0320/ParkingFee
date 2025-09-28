package com.naze.parkingfee.domain.usecase

import com.naze.parkingfee.domain.model.ParkingSession
import com.naze.parkingfee.domain.repository.ParkingRepository
import javax.inject.Inject

/**
 * 주차 종료 UseCase
 */
class StopParkingUseCase @Inject constructor(
    private val parkingRepository: ParkingRepository
) {
    suspend operator fun invoke(sessionId: String): ParkingSession {
        return parkingRepository.stopParkingSession(sessionId)
    }
    
    suspend fun execute(sessionId: String): ParkingSession = invoke(sessionId)
}
