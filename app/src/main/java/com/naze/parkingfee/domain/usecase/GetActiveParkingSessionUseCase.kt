package com.naze.parkingfee.domain.usecase

import com.naze.parkingfee.domain.model.ParkingSession
import com.naze.parkingfee.domain.repository.ParkingRepository
import javax.inject.Inject

/**
 * 활성 주차 세션 조회 UseCase
 */
class GetActiveParkingSessionUseCase @Inject constructor(
    private val parkingRepository: ParkingRepository
) {
    suspend operator fun invoke(): ParkingSession? {
        return parkingRepository.getActiveParkingSession()
    }
    
    suspend fun execute(): ParkingSession? = invoke()
}
