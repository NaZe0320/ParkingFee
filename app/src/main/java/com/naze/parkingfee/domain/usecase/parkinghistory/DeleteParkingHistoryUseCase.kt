package com.naze.parkingfee.domain.usecase.parkinghistory

import com.naze.parkingfee.domain.repository.ParkingHistoryRepository
import javax.inject.Inject

/**
 * 주차 기록 삭제 UseCase
 */
class DeleteParkingHistoryUseCase @Inject constructor(
    private val historyRepository: ParkingHistoryRepository
) {
    suspend fun execute(historyId: String) {
        historyRepository.deleteParkingHistory(historyId)
    }
}

