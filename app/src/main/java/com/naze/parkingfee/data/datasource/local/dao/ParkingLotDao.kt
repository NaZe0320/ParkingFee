package com.naze.parkingfee.data.datasource.local.dao

import androidx.room.*
import com.naze.parkingfee.data.datasource.local.entity.ParkingLotEntity
import kotlinx.coroutines.flow.Flow

/**
 * 주차장 데이터 접근 객체
 */
@Dao
interface ParkingLotDao {
    
    @Query("SELECT * FROM parking_lots WHERE isActive = 1 ORDER BY createdAt ASC")
    suspend fun getAllParkingLots(): List<ParkingLotEntity>
    
    @Query("SELECT * FROM parking_lots WHERE isActive = 1 ORDER BY createdAt ASC")
    fun observeParkingLots(): Flow<List<ParkingLotEntity>>
    
    @Query("SELECT * FROM parking_lots WHERE id = :lotId")
    suspend fun getParkingLot(lotId: String): ParkingLotEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParkingLot(parkingLot: ParkingLotEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParkingLots(parkingLots: List<ParkingLotEntity>)
    
    @Update
    suspend fun updateParkingLot(parkingLot: ParkingLotEntity)
    
    @Delete
    suspend fun deleteParkingLot(parkingLot: ParkingLotEntity)
    
    @Query("UPDATE parking_lots SET isActive = 0 WHERE id = :lotId")
    suspend fun deleteParkingLotById(lotId: String)
}
