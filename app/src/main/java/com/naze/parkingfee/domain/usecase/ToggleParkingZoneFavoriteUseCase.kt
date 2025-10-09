package com.naze.parkingfee.domain.usecase

import com.naze.parkingfee.domain.repository.ParkingRepository
import javax.inject.Inject

/**
 * 주차 구역 즐겨찾기 토글 UseCase
 */
class ToggleParkingZoneFavoriteUseCase @Inject constructor(
    private val parkingRepository: ParkingRepository
) {
    
    /**
     * 주차 구역의 즐겨찾기 상태를 토글합니다.
     * @param zoneId 주차 구역 ID
     * @return 토글 성공 여부
     */
    suspend operator fun invoke(zoneId: String): Boolean {
        return parkingRepository.toggleParkingZoneFavorite(zoneId)
    }
}
