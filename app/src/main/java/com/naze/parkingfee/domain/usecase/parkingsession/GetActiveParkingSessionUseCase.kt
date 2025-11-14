package com.naze.parkingfee.domain.usecase.parkingsession

import com.naze.parkingfee.domain.model.ParkingSession
import com.naze.parkingfee.domain.repository.ParkingRepository
import javax.inject.Inject

/**
 * 활성 주차 세션 조회 UseCase
 */
class GetActiveParkingSessionUseCase @Inject constructor(
    private val parkingRepository: ParkingRepository
) {
    suspend fun execute(): ParkingSession? {
        return parkingRepository.getActiveParkingSession()
    }
}

