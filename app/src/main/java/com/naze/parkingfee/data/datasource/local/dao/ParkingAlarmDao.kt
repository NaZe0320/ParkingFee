package com.naze.parkingfee.data.datasource.local.dao

import androidx.room.*
import com.naze.parkingfee.data.datasource.local.entity.ParkingAlarmEntity
import kotlinx.coroutines.flow.Flow

/**
 * 주차 알람 데이터 접근 객체
 */
@Dao
interface ParkingAlarmDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: ParkingAlarmEntity)
    
    @Delete
    suspend fun delete(alarm: ParkingAlarmEntity)
    
    @Query("DELETE FROM parking_alarms WHERE id = :alarmId")
    suspend fun deleteById(alarmId: String)
    
    @Query("SELECT * FROM parking_alarms WHERE sessionId = :sessionId ORDER BY scheduledTime ASC")
    suspend fun getBySessionId(sessionId: String): List<ParkingAlarmEntity>
    
    @Query("SELECT * FROM parking_alarms WHERE sessionId = :sessionId ORDER BY scheduledTime ASC")
    fun observeBySessionId(sessionId: String): Flow<List<ParkingAlarmEntity>>
    
    @Query("UPDATE parking_alarms SET isTriggered = :isTriggered WHERE id = :alarmId")
    suspend fun updateTriggered(alarmId: String, isTriggered: Boolean)
    
    @Query("DELETE FROM parking_alarms WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: String)
    
    @Query("SELECT * FROM parking_alarms WHERE id = :alarmId")
    suspend fun getById(alarmId: String): ParkingAlarmEntity?
}

