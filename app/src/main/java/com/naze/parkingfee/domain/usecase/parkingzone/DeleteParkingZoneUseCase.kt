package com.naze.parkingfee.domain.usecase.parkingzone

import com.naze.parkingfee.domain.repository.ParkingRepository
import javax.inject.Inject

/**
 * 주차 구역 삭제 UseCase
 */
class DeleteParkingZoneUseCase @Inject constructor(
    private val parkingRepository: ParkingRepository
) {
    suspend fun execute(zoneId: String): Boolean {
        return parkingRepository.deleteParkingZone(zoneId)
    }
}

