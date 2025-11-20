package com.naze.parkingfee.domain.usecase.parkingzone

import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.domain.repository.ParkingRepository
import javax.inject.Inject

/**
 * 주차 구역 업데이트 UseCase
 */
class UpdateParkingZoneUseCase @Inject constructor(
    private val parkingRepository: ParkingRepository
) {
    suspend fun execute(zone: ParkingZone): ParkingZone {
        return parkingRepository.updateParkingZone(zone)
    }
}

