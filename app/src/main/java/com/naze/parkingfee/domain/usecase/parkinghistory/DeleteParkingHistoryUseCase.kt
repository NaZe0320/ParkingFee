package com.naze.parkingfee.domain.usecase.parkinghistory

import com.naze.parkingfee.domain.repository.AuthRepository
import com.naze.parkingfee.domain.repository.ParkingHistoryRepository
import com.naze.parkingfee.utils.SyncLogger
import javax.inject.Inject

/**
 * 주차 기록 삭제 UseCase
 */
class DeleteParkingHistoryUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val historyRepository: ParkingHistoryRepository
) {
    suspend fun execute(historyId: String) {
        val authResult = authRepository.ensureSignedInAnonymously()
        if (authResult.isSuccess) {
            SyncLogger.logSyncAttempt(
                entityType = "ParkingHistoryDelete",
                entityId = historyId,
                uid = authRepository.getCurrentUid()
            )
        } else {
            SyncLogger.logSyncSkipped(
                entityType = "ParkingHistoryDelete",
                entityId = historyId,
                reason = authResult.exceptionOrNull()?.message ?: "ensureSignedInAnonymously failed"
            )
        }

        historyRepository.deleteParkingHistory(historyId)
    }
}

