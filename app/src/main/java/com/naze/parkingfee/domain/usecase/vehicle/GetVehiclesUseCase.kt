package com.naze.parkingfee.domain.usecase.vehicle

import com.naze.parkingfee.domain.model.vehicle.Vehicle
import com.naze.parkingfee.domain.repository.VehicleRepository
import javax.inject.Inject

/**
 * 모든 차량 조회 UseCase
 */
class GetVehiclesUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    suspend fun execute(): List<Vehicle> {
        return vehicleRepository.getAllVehicles()
    }
}
