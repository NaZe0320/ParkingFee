package com.naze.parkingfee.domain.usecase.parkinghistory

import com.naze.parkingfee.domain.model.ParkingHistory
import com.naze.parkingfee.domain.repository.ParkingHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 주차 기록 목록 조회 UseCase
 */
class GetParkingHistoryUseCase @Inject constructor(
    private val historyRepository: ParkingHistoryRepository
) {
    fun execute(): Flow<List<ParkingHistory>> {
        return historyRepository.getAllParkingHistories()
    }
}

