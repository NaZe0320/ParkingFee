package com.naze.parkingfee.data.datasource.remote.firestore

import com.naze.parkingfee.domain.model.ParkingZone

/**
 * (스켈레톤) ParkingZone Firestore 데이터 소스
 *
 * 지금은 실제 Firestore 연동 전 단계로, 인터페이스만 제공한다.
 */
interface ParkingZoneRemoteDataSource {
    suspend fun upsertParkingZone(zone: ParkingZone): Result<Unit>
    suspend fun deleteParkingZone(zoneId: String): Result<Unit>
    suspend fun fetchParkingZones(): Result<List<ParkingZone>>
}

