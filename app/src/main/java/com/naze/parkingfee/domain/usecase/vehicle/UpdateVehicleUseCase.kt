package com.naze.parkingfee.domain.usecase.vehicle

import com.naze.parkingfee.domain.model.vehicle.Vehicle
import com.naze.parkingfee.domain.repository.VehicleRepository
import javax.inject.Inject

/**
 * 차량 수정 UseCase
 */
class UpdateVehicleUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    suspend fun execute(vehicle: Vehicle): Result<Vehicle> {
        return vehicleRepository.updateVehicle(vehicle)
    }
}
