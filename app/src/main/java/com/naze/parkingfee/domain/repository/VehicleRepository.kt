package com.naze.parkingfee.domain.repository

import com.naze.parkingfee.domain.model.vehicle.Vehicle
import kotlinx.coroutines.flow.Flow

/**
 * 차량 리포지토리 인터페이스
 */
interface VehicleRepository {
    
    /**
     * 모든 차량 조회
     */
    suspend fun getAllVehicles(): List<Vehicle>
    
    /**
     * 모든 차량 조회 (Flow)
     */
    fun observeAllVehicles(): Flow<List<Vehicle>>
    
    /**
     * 차량 ID로 조회
     */
    suspend fun getVehicleById(id: String): Vehicle?
    
    /**
     * 차량 추가
     */
    suspend fun addVehicle(vehicle: Vehicle): Result<Vehicle>
    
    /**
     * 차량 수정
     */
    suspend fun updateVehicle(vehicle: Vehicle): Result<Vehicle>
    
    /**
     * 차량 삭제
     */
    suspend fun deleteVehicle(id: String): Result<Unit>
    
    /**
     * 차량 개수 조회
     */
    suspend fun getVehicleCount(): Int
    
    /**
     * 최대 차량 개수 확인 (3대 제한)
     */
    suspend fun canAddVehicle(): Boolean {
        return getVehicleCount() < 3
    }
}
