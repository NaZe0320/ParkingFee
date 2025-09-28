package com.naze.parkingfee.domain.repository

import com.naze.parkingfee.domain.model.ParkingLot
import kotlinx.coroutines.flow.Flow

/**
 * 주차장 관련 데이터 접근을 위한 Repository 인터페이스
 */
interface ParkingLotRepository {
    
    /**
     * 주차장을 추가합니다.
     */
    suspend fun addParkingLot(parkingLot: ParkingLot): ParkingLot
    
    /**
     * 모든 주차장을 조회합니다.
     */
    suspend fun getParkingLots(): List<ParkingLot>
    
    /**
     * 주차장을 실시간으로 관찰합니다.
     */
    fun observeParkingLots(): Flow<List<ParkingLot>>
    
    /**
     * 특정 주차장을 조회합니다.
     */
    suspend fun getParkingLot(lotId: String): ParkingLot?
    
    /**
     * 주차장을 업데이트합니다.
     */
    suspend fun updateParkingLot(parkingLot: ParkingLot): ParkingLot
    
    /**
     * 주차장을 삭제합니다.
     */
    suspend fun deleteParkingLot(lotId: String): Boolean
}
