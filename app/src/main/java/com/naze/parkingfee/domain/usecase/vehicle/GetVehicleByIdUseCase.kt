package com.naze.parkingfee.domain.usecase.vehicle

import com.naze.parkingfee.domain.model.vehicle.Vehicle
import com.naze.parkingfee.domain.repository.VehicleRepository
import javax.inject.Inject

/**
 * 차량 ID로 조회 UseCase
 */
class GetVehicleByIdUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    suspend fun execute(vehicleId: String): Vehicle? {
        return vehicleRepository.getVehicleById(vehicleId)
    }
}

