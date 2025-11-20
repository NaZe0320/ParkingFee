package com.naze.parkingfee.domain.usecase.selectedvehicle

import com.naze.parkingfee.domain.repository.SelectedVehicleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 선택된 차량 ID 조회 UseCase
 */
class GetSelectedVehicleIdUseCase @Inject constructor(
    private val selectedVehicleRepository: SelectedVehicleRepository
) {
    fun execute(): Flow<String?> {
        return selectedVehicleRepository.selectedVehicleId
    }
}

