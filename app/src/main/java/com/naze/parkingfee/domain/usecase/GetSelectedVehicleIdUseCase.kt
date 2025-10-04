package com.naze.parkingfee.domain.usecase

import com.naze.parkingfee.domain.repository.SelectedVehicleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 선택된 차량 ID를 가져오는 UseCase
 */
class GetSelectedVehicleIdUseCase @Inject constructor(
    private val selectedVehicleRepository: SelectedVehicleRepository
) {
    
    /**
     * 선택된 차량 ID를 관찰합니다.
     */
    fun execute(): Flow<String?> = selectedVehicleRepository.selectedVehicleId
}
