package com.naze.parkingfee.data.datasource.local.dao

import androidx.room.*
import com.naze.parkingfee.data.datasource.local.entity.ParkingHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 주차 기록 DAO
 */
@Dao
interface ParkingHistoryDao {
    
    @Query("SELECT * FROM parking_history ORDER BY startedAt DESC")
    fun getAllParkingHistories(): Flow<List<ParkingHistoryEntity>>
    
    @Query("SELECT * FROM parking_history WHERE id = :id")
    suspend fun getParkingHistoryById(id: String): ParkingHistoryEntity?
    
    @Query("SELECT * FROM parking_history WHERE zoneId = :zoneId ORDER BY startedAt DESC")
    fun getParkingHistoriesByZoneId(zoneId: String): Flow<List<ParkingHistoryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParkingHistory(history: ParkingHistoryEntity)
    
    @Delete
    suspend fun deleteParkingHistory(history: ParkingHistoryEntity)
    
    @Query("DELETE FROM parking_history WHERE id = :id")
    suspend fun deleteParkingHistoryById(id: String)
    
    @Query("DELETE FROM parking_history")
    suspend fun deleteAllParkingHistories()
}
