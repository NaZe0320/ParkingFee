package com.naze.parkingfee.domain.usecase

import com.naze.parkingfee.domain.model.ParkingSession
import com.naze.parkingfee.domain.repository.ParkingRepository
import javax.inject.Inject

/**
 * 주차 시작 UseCase
 */
class StartParkingUseCase @Inject constructor(
    private val parkingRepository: ParkingRepository
) {
    suspend operator fun invoke(zoneId: String): ParkingSession {
        return parkingRepository.startParkingSession(zoneId)
    }
    
    suspend fun execute(zoneId: String): ParkingSession = invoke(zoneId)
}
