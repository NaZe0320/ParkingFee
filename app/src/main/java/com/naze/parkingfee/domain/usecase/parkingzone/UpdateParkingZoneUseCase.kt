package com.naze.parkingfee.domain.usecase.parkingzone

import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.domain.repository.AuthRepository
import com.naze.parkingfee.domain.repository.ParkingRepository
import com.naze.parkingfee.utils.SyncLogger
import javax.inject.Inject

/**
 * 주차 구역 업데이트 UseCase
 */
class UpdateParkingZoneUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val parkingRepository: ParkingRepository
) {
    suspend fun execute(zone: ParkingZone): ParkingZone {
        val authResult = authRepository.ensureSignedInAnonymously()
        if (authResult.isSuccess) {
            SyncLogger.logSyncAttempt(
                entityType = "ParkingZoneUpdate",
                entityId = zone.id,
                uid = authRepository.getCurrentUid()
            )
        } else {
            SyncLogger.logSyncSkipped(
                entityType = "ParkingZoneUpdate",
                entityId = zone.id,
                reason = authResult.exceptionOrNull()?.message ?: "ensureSignedInAnonymously failed"
            )
        }

        return parkingRepository.updateParkingZone(zone)
    }
}

