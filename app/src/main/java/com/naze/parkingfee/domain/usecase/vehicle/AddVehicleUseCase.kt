package com.naze.parkingfee.domain.usecase.vehicle

import com.naze.parkingfee.domain.model.vehicle.Vehicle
import com.naze.parkingfee.domain.repository.VehicleRepository
import javax.inject.Inject

/**
 * 차량 추가 유즈케이스
 */
class AddVehicleUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(vehicle: Vehicle): Result<Vehicle> {
        return vehicleRepository.addVehicle(vehicle)
    }
}
