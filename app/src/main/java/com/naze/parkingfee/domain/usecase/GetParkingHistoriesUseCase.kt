package com.naze.parkingfee.domain.usecase

import com.naze.parkingfee.domain.model.ParkingHistory
import com.naze.parkingfee.domain.repository.ParkingHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 주차 기록 목록 조회 UseCase
 */
class GetParkingHistoriesUseCase @Inject constructor(
    private val repository: ParkingHistoryRepository
) {
    fun execute(): Flow<List<ParkingHistory>> {
        return repository.getAllParkingHistories()
    }
}
