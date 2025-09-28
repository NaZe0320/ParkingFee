package com.naze.parkingfee.data.datasource.local.dao

import androidx.room.*
import com.naze.parkingfee.data.datasource.local.entity.ParkingZoneEntity
import com.naze.parkingfee.data.datasource.local.entity.ParkingSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 주차 관련 데이터 접근 객체
 */
@Dao
interface ParkingDao {
    
    // ParkingZone 관련 쿼리
    @Query("SELECT * FROM parking_zones WHERE isActive = 1")
    suspend fun getAllParkingZones(): List<ParkingZoneEntity>
    
    @Query("SELECT * FROM parking_zones WHERE isActive = 1")
    fun observeParkingZones(): Flow<List<ParkingZoneEntity>>
    
    @Query("SELECT * FROM parking_zones WHERE id = :zoneId")
    suspend fun getParkingZone(zoneId: String): ParkingZoneEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParkingZone(parkingZone: ParkingZoneEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParkingZones(parkingZones: List<ParkingZoneEntity>)
    
    @Update
    suspend fun updateParkingZone(parkingZone: ParkingZoneEntity)
    
    @Delete
    suspend fun deleteParkingZone(parkingZone: ParkingZoneEntity)
    
    // ParkingSession 관련 쿼리
    @Query("SELECT * FROM parking_sessions WHERE isActive = 1 ORDER BY startTime DESC")
    suspend fun getAllParkingSessions(): List<ParkingSessionEntity>
    
    @Query("SELECT * FROM parking_sessions WHERE isActive = 1 ORDER BY startTime DESC")
    fun observeParkingSessions(): Flow<List<ParkingSessionEntity>>
    
    @Query("SELECT * FROM parking_sessions WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveParkingSession(): ParkingSessionEntity?
    
    @Query("SELECT * FROM parking_sessions WHERE isActive = 1 LIMIT 1")
    fun observeActiveParkingSession(): Flow<ParkingSessionEntity?>
    
    @Query("SELECT * FROM parking_sessions WHERE id = :sessionId")
    suspend fun getParkingSession(sessionId: String): ParkingSessionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParkingSession(parkingSession: ParkingSessionEntity)
    
    @Update
    suspend fun updateParkingSession(parkingSession: ParkingSessionEntity)
    
    @Delete
    suspend fun deleteParkingSession(parkingSession: ParkingSessionEntity)
}
