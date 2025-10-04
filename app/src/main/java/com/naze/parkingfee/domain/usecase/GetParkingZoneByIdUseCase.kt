package com.naze.parkingfee.domain.usecase

import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.domain.repository.ParkingRepository
import javax.inject.Inject

/**
 * ID로 주차 구역 조회 UseCase
 */
class GetParkingZoneByIdUseCase @Inject constructor(
    private val repository: ParkingRepository
) {
    suspend fun execute(zoneId: String): ParkingZone? {
        return repository.getParkingZoneById(zoneId)
    }
}
