package com.naze.parkingfee.data.datasource.remote.firestore

import com.naze.parkingfee.domain.model.ParkingHistory

/**
 * (스켈레톤) ParkingHistory Firestore 데이터 소스
 *
 * 지금은 실제 Firestore 연동 전 단계로, 인터페이스만 제공한다.
 */
interface ParkingHistoryRemoteDataSource {
    suspend fun upsertParkingHistory(history: ParkingHistory): Result<Unit>
    suspend fun deleteParkingHistory(historyId: String): Result<Unit>
    suspend fun fetchParkingHistories(): Result<List<ParkingHistory>>
}

