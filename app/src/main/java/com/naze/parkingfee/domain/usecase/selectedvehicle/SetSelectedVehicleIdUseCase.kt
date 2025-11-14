package com.naze.parkingfee.domain.usecase.selectedvehicle

import com.naze.parkingfee.domain.repository.SelectedVehicleRepository
import javax.inject.Inject

/**
 * 선택된 차량 ID 설정 UseCase
 */
class SetSelectedVehicleIdUseCase @Inject constructor(
    private val selectedVehicleRepository: SelectedVehicleRepository
) {
    suspend fun execute(vehicleId: String?) {
        selectedVehicleRepository.setSelectedVehicleId(vehicleId)
    }
}

