package com.naze.parkingfee.domain.usecase.parkinghistory

import com.naze.parkingfee.domain.model.ParkingHistory
import com.naze.parkingfee.domain.repository.ParkingHistoryRepository
import javax.inject.Inject

/**
 * 주차 기록 저장 UseCase
 */
class SaveParkingHistoryUseCase @Inject constructor(
    private val historyRepository: ParkingHistoryRepository
) {
    suspend fun execute(history: ParkingHistory) {
        historyRepository.saveParkingHistory(history)
    }
}

