package com.naze.parkingfee.domain.usecase.vehicle

import com.naze.parkingfee.domain.repository.VehicleRepository
import javax.inject.Inject

/**
 * 차량 삭제 UseCase
 */
class DeleteVehicleUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    suspend fun execute(vehicleId: String): Result<Unit> {
        return vehicleRepository.deleteVehicle(vehicleId)
    }
}
