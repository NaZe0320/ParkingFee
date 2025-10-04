package com.naze.parkingfee.domain.usecase

import com.naze.parkingfee.domain.repository.SelectedVehicleRepository
import javax.inject.Inject

/**
 * 선택된 차량 ID를 설정하는 UseCase
 */
class SetSelectedVehicleIdUseCase @Inject constructor(
    private val selectedVehicleRepository: SelectedVehicleRepository
) {
    
    /**
     * 선택된 차량 ID를 설정합니다.
     */
    suspend fun execute(vehicleId: String?) {
        selectedVehicleRepository.setSelectedVehicleId(vehicleId)
    }
}
