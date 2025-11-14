package com.naze.parkingfee.domain.usecase.parkingzone

import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.domain.repository.ParkingRepository
import javax.inject.Inject

/**
 * 주차 구역 조회 UseCase
 */
class GetParkingZonesUseCase @Inject constructor(
    private val parkingRepository: ParkingRepository
) {
    suspend fun execute(): List<ParkingZone> {
        return parkingRepository.getParkingZones()
    }
}

