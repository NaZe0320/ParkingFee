package com.naze.parkingfee.domain.usecase.parkingzone

import com.naze.parkingfee.domain.repository.ParkingRepository
import javax.inject.Inject

/**
 * 주차 구역 즐겨찾기 토글 UseCase
 */
class ToggleParkingZoneFavoriteUseCase @Inject constructor(
    private val parkingRepository: ParkingRepository
) {
    suspend fun execute(zoneId: String): Boolean {
        return parkingRepository.toggleParkingZoneFavorite(zoneId)
    }
}

