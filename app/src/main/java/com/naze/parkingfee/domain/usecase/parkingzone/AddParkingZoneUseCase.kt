package com.naze.parkingfee.domain.usecase.parkingzone

import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.domain.repository.AuthRepository
import com.naze.parkingfee.domain.repository.ParkingRepository
import com.naze.parkingfee.utils.SyncLogger
import javax.inject.Inject

/**
 * 주차 구역 추가 UseCase
 */
class AddParkingZoneUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val parkingRepository: ParkingRepository
) {
    suspend fun execute(parkingZone: ParkingZone): ParkingZone {
        val authResult = authRepository.ensureSignedInAnonymously()
        if (authResult.isSuccess) {
            SyncLogger.logSyncAttempt(
                entityType = "ParkingZone",
                entityId = parkingZone.id,
                uid = authRepository.getCurrentUid()
            )
        } else {
            SyncLogger.logSyncSkipped(
                entityType = "ParkingZone",
                entityId = parkingZone.id,
                reason = authResult.exceptionOrNull()?.message ?: "ensureSignedInAnonymously failed"
            )
        }

        return parkingRepository.addParkingZone(parkingZone)
    }
}

