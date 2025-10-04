package com.naze.parkingfee.data.repository

import com.naze.parkingfee.data.datasource.local.dao.VehicleDao
import com.naze.parkingfee.data.mapper.VehicleMapper
import com.naze.parkingfee.domain.model.vehicle.Vehicle
import com.naze.parkingfee.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 차량 리포지토리 구현체
 */
@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val vehicleDao: VehicleDao
) : VehicleRepository {
    
    override suspend fun getAllVehicles(): List<Vehicle> {
        val entities = vehicleDao.getAllVehicles()
        return VehicleMapper.toDomainList(entities)
    }
    
    override fun observeAllVehicles(): Flow<List<Vehicle>> {
        return vehicleDao.observeAllVehicles().map { entities ->
            VehicleMapper.toDomainList(entities)
        }
    }
    
    override suspend fun getVehicleById(id: String): Vehicle? {
        val entity = vehicleDao.getVehicleById(id)
        return entity?.let { VehicleMapper.toDomain(it) }
    }
    
    override suspend fun addVehicle(vehicle: Vehicle): Result<Vehicle> {
        return try {
            // 최대 3대 제한 확인
            if (!canAddVehicle()) {
                return Result.failure(Exception("최대 3대까지만 등록할 수 있습니다."))
            }
            
            val entity = VehicleMapper.toEntity(vehicle)
            vehicleDao.insertVehicle(entity)
            Result.success(vehicle)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateVehicle(vehicle: Vehicle): Result<Vehicle> {
        return try {
            val entity = VehicleMapper.toEntity(vehicle)
            vehicleDao.updateVehicle(entity)
            Result.success(vehicle)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteVehicle(id: String): Result<Unit> {
        return try {
            vehicleDao.deleteVehicleById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getVehicleCount(): Int {
        return vehicleDao.getVehicleCount()
    }
}
