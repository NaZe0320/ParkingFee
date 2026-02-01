package com.naze.parkingfee.domain.usecase.parkinghistory

import com.naze.parkingfee.domain.model.ParkingHistory
import com.naze.parkingfee.domain.repository.AuthRepository
import com.naze.parkingfee.domain.repository.ParkingHistoryRepository
import com.naze.parkingfee.utils.SyncLogger
import javax.inject.Inject

/**
 * 주차 기록 저장 UseCase
 */
class SaveParkingHistoryUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val historyRepository: ParkingHistoryRepository
) {
    suspend fun execute(history: ParkingHistory) {
        val authResult = authRepository.ensureSignedInAnonymously()
        if (authResult.isSuccess) {
            SyncLogger.logSyncAttempt(
                entityType = "ParkingHistory",
                entityId = history.id,
                uid = authRepository.getCurrentUid()
            )
        } else {
            SyncLogger.logSyncSkipped(
                entityType = "ParkingHistory",
                entityId = history.id,
                reason = authResult.exceptionOrNull()?.message ?: "ensureSignedInAnonymously failed"
            )
        }

        historyRepository.saveParkingHistory(history)
    }
}

