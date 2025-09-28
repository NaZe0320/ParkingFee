package com.naze.parkingfee.domain.repository

import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.domain.model.ParkingSession
import kotlinx.coroutines.flow.Flow

/**
 * 주차 관련 데이터 접근을 위한 Repository 인터페이스
 */
interface ParkingRepository {
    
    /**
     * 모든 주차 구역을 조회합니다.
     */
    suspend fun getParkingZones(): List<ParkingZone>
    
    /**
     * 주차 구역을 실시간으로 관찰합니다.
     */
    fun observeParkingZones(): Flow<List<ParkingZone>>
    
    /**
     * 특정 주차 구역을 조회합니다.
     */
    suspend fun getParkingZone(zoneId: String): ParkingZone?
    
    /**
     * 주차 구역을 추가합니다.
     */
    suspend fun addParkingZone(parkingZone: ParkingZone): ParkingZone
    
    /**
     * 주차 구역을 업데이트합니다.
     */
    suspend fun updateParkingZone(parkingZone: ParkingZone): ParkingZone
    
    /**
     * 주차 구역을 삭제합니다.
     */
    suspend fun deleteParkingZone(zoneId: String): Boolean
    
    /**
     * 주차 세션을 시작합니다.
     */
    suspend fun startParkingSession(zoneId: String): ParkingSession
    
    /**
     * 주차 세션을 종료합니다.
     */
    suspend fun stopParkingSession(sessionId: String): ParkingSession
    
    /**
     * 활성 주차 세션을 조회합니다.
     */
    suspend fun getActiveParkingSession(): ParkingSession?
    
    /**
     * 활성 주차 세션을 실시간으로 관찰합니다.
     */
    fun observeActiveParkingSession(): Flow<ParkingSession?>
    
    /**
     * 주차 세션 목록을 조회합니다.
     */
    suspend fun getParkingSessions(): List<ParkingSession>
    
    /**
     * 주차 세션을 실시간으로 관찰합니다.
     */
    fun observeParkingSessions(): Flow<List<ParkingSession>>
}
