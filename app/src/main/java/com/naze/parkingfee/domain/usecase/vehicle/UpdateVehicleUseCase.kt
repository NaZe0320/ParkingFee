package com.naze.parkingfee.domain.usecase.vehicle

import com.naze.parkingfee.domain.model.vehicle.Vehicle
import com.naze.parkingfee.domain.repository.AuthRepository
import com.naze.parkingfee.domain.repository.VehicleRepository
import com.naze.parkingfee.utils.SyncLogger
import javax.inject.Inject

/**
 * 차량 수정 UseCase
 */
class UpdateVehicleUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val vehicleRepository: VehicleRepository
) {
    suspend fun execute(vehicle: Vehicle): Result<Vehicle> {
        val authResult = authRepository.ensureSignedInAnonymously()
        if (authResult.isSuccess) {
            SyncLogger.logSyncAttempt(
                entityType = "Vehicle",
                entityId = vehicle.id,
                uid = authRepository.getCurrentUid()
            )
        } else {
            SyncLogger.logSyncSkipped(
                entityType = "Vehicle",
                entityId = vehicle.id,
                reason = authResult.exceptionOrNull()?.message ?: "ensureSignedInAnonymously failed"
            )
        }

        return vehicleRepository.updateVehicle(vehicle)
    }
}
