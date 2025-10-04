package com.naze.parkingfee.domain.usecase.vehicle

import com.naze.parkingfee.domain.repository.VehicleRepository
import javax.inject.Inject

/**
 * 차량 삭제 유즈케이스
 */
class DeleteVehicleUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(vehicleId: String): Result<Unit> {
        return vehicleRepository.deleteVehicle(vehicleId)
    }
}
