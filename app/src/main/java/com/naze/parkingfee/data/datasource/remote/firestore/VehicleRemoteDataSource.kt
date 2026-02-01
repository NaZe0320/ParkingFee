package com.naze.parkingfee.data.datasource.remote.firestore

import com.naze.parkingfee.domain.model.vehicle.Vehicle

/**
 * (스켈레톤) Vehicle Firestore 데이터 소스
 *
 * 지금은 실제 Firestore 연동 전 단계로, 인터페이스만 제공한다.
 */
interface VehicleRemoteDataSource {
    suspend fun upsertVehicle(vehicle: Vehicle): Result<Unit>
    suspend fun deleteVehicle(vehicleId: String): Result<Unit>
    suspend fun fetchVehicles(): Result<List<Vehicle>>
}

