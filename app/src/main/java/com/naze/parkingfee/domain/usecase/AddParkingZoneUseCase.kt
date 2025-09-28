package com.naze.parkingfee.domain.usecase

import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.domain.repository.ParkingRepository
import javax.inject.Inject

/**
 * 주차 구역 추가 UseCase
 */
class AddParkingZoneUseCase @Inject constructor(
    private val parkingRepository: ParkingRepository
) {
    suspend operator fun invoke(parkingZone: ParkingZone): ParkingZone {
        return parkingRepository.addParkingZone(parkingZone)
    }
    
    suspend fun execute(parkingZone: ParkingZone): ParkingZone = invoke(parkingZone)
}
