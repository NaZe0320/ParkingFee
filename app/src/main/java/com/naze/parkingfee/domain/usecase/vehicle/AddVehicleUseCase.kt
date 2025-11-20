package com.naze.parkingfee.domain.usecase.vehicle

import com.naze.parkingfee.domain.model.vehicle.Vehicle
import com.naze.parkingfee.domain.repository.VehicleRepository
import javax.inject.Inject

/**
 * 차량 추가 UseCase
 */
class AddVehicleUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    suspend fun execute(vehicle: Vehicle): Result<Vehicle> {
        return vehicleRepository.addVehicle(vehicle)
    }
}
