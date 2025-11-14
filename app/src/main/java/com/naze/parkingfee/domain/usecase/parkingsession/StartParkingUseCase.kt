package com.naze.parkingfee.domain.usecase.parkingsession

import com.naze.parkingfee.domain.model.ParkingSession
import com.naze.parkingfee.domain.repository.ParkingRepository
import javax.inject.Inject

/**
 * 주차 시작 UseCase
 */
class StartParkingUseCase @Inject constructor(
    private val parkingRepository: ParkingRepository
) {
    suspend fun execute(zoneId: String): ParkingSession {
        return parkingRepository.startParkingSession(zoneId)
    }
}

