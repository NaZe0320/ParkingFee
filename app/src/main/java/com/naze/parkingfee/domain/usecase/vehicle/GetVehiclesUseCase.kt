package com.naze.parkingfee.domain.usecase.vehicle

import com.naze.parkingfee.domain.model.vehicle.Vehicle
import com.naze.parkingfee.domain.repository.VehicleRepository
import javax.inject.Inject

/**
 * 모든 차량 조회 유즈케이스
 */
class GetVehiclesUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(): List<Vehicle> {
        return vehicleRepository.getAllVehicles()
    }
}
