package com.naze.parkingfee.data.datasource.local.dao

import androidx.room.*
import com.naze.parkingfee.data.datasource.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

/**
 * 차량 데이터 접근 객체
 */
@Dao
interface VehicleDao {
    
    /**
     * 모든 차량 조회
     */
    @Query("SELECT * FROM vehicles ORDER BY createdAt ASC")
    suspend fun getAllVehicles(): List<VehicleEntity>
    
    /**
     * 모든 차량 조회 (Flow)
     */
    @Query("SELECT * FROM vehicles ORDER BY createdAt ASC")
    fun observeAllVehicles(): Flow<List<VehicleEntity>>
    
    /**
     * 차량 ID로 조회
     */
    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleById(id: String): VehicleEntity?
    
    /**
     * 차량 추가
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity)
    
    /**
     * 차량 수정
     */
    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)
    
    /**
     * 차량 삭제
     */
    @Delete
    suspend fun deleteVehicle(vehicle: VehicleEntity)
    
    /**
     * 차량 ID로 삭제
     */
    @Query("DELETE FROM vehicles WHERE id = :id")
    suspend fun deleteVehicleById(id: String)
    
    /**
     * 차량 개수 조회
     */
    @Query("SELECT COUNT(*) FROM vehicles")
    suspend fun getVehicleCount(): Int
}
