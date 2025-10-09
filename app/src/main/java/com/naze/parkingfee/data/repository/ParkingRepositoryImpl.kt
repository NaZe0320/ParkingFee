package com.naze.parkingfee.data.repository

import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.domain.model.ParkingSession
import com.naze.parkingfee.domain.repository.ParkingRepository
import com.naze.parkingfee.data.datasource.local.dao.ParkingDao
import com.naze.parkingfee.data.mapper.ParkingZoneMapper
import com.naze.parkingfee.data.mapper.ParkingSessionMapper
import com.naze.parkingfee.utils.FeeCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 주차 Repository 구현체
 */
class ParkingRepositoryImpl @Inject constructor(
    private val parkingDao: ParkingDao,
    private val parkingZoneMapper: ParkingZoneMapper,
    private val parkingSessionMapper: ParkingSessionMapper
) : ParkingRepository {

    override suspend fun getParkingZones(): List<ParkingZone> {
        return parkingDao.getAllParkingZones().map { entity ->
            parkingZoneMapper.mapToDomain(entity)
        }
    }

    override fun observeParkingZones(): Flow<List<ParkingZone>> {
        return parkingDao.observeParkingZones().map { entities ->
            entities.map { entity ->
                parkingZoneMapper.mapToDomain(entity)
            }
        }
    }

    override suspend fun getParkingZone(zoneId: String): ParkingZone? {
        return parkingDao.getParkingZone(zoneId)?.let { entity ->
            parkingZoneMapper.mapToDomain(entity)
        }
    }
    
    override suspend fun getParkingZoneById(zoneId: String): ParkingZone? {
        return parkingDao.getParkingZone(zoneId)?.let { entity ->
            parkingZoneMapper.mapToDomain(entity)
        }
    }

    override suspend fun addParkingZone(parkingZone: ParkingZone): ParkingZone {
        val entity = parkingZoneMapper.mapToEntity(parkingZone)
        parkingDao.insertParkingZone(entity)
        return parkingZone
    }

    override suspend fun updateParkingZone(parkingZone: ParkingZone): ParkingZone {
        val entity = parkingZoneMapper.mapToEntity(parkingZone)
        parkingDao.updateParkingZone(entity)
        return parkingZone
    }

    override suspend fun toggleParkingZoneFavorite(zoneId: String): Boolean {
        return try {
            val entity = parkingDao.getParkingZone(zoneId)
                ?: return false
            
            val updatedEntity = entity.copy(isFavorite = !entity.isFavorite)
            parkingDao.updateParkingZone(updatedEntity)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteParkingZone(zoneId: String): Boolean {
        return try {
            parkingDao.deleteParkingZoneById(zoneId)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun startParkingSession(zoneId: String): ParkingSession {
        val sessionId = generateSessionId()
        val startTime = System.currentTimeMillis()
        
        val sessionEntity = com.naze.parkingfee.data.datasource.local.entity.ParkingSessionEntity(
            id = sessionId,
            zoneId = zoneId,
            startTime = startTime,
            isActive = true
        )
        
        parkingDao.insertParkingSession(sessionEntity)
        
        return parkingSessionMapper.mapToDomain(sessionEntity)
    }

    override suspend fun stopParkingSession(sessionId: String): ParkingSession {
        val sessionEntity = parkingDao.getParkingSession(sessionId)
            ?: throw IllegalArgumentException("Session not found: $sessionId")
        
        val endTime = System.currentTimeMillis()
        
        // 구역 정보 조회하여 FeeCalculator로 요금 계산
        val zone = parkingDao.getParkingZone(sessionEntity.zoneId)?.let { entity ->
            parkingZoneMapper.mapToDomain(entity)
        }
        val computedFee = zone?.let { 
            FeeCalculator.calculateFeeForZone(sessionEntity.startTime, endTime, it) 
        } ?: 0.0
        
        val updatedEntity = sessionEntity.copy(
            endTime = endTime,
            isActive = false,
            totalFee = computedFee
        )
        
        parkingDao.updateParkingSession(updatedEntity)
        
        return parkingSessionMapper.mapToDomain(updatedEntity)
    }

    override suspend fun getActiveParkingSession(): ParkingSession? {
        return parkingDao.getActiveParkingSession()?.let { entity ->
            parkingSessionMapper.mapToDomain(entity)
        }
    }

    override fun observeActiveParkingSession(): Flow<ParkingSession?> {
        return parkingDao.observeActiveParkingSession().map { entity ->
            entity?.let { parkingSessionMapper.mapToDomain(it) }
        }
    }

    override suspend fun getParkingSessions(): List<ParkingSession> {
        return parkingDao.getAllParkingSessions().map { entity ->
            parkingSessionMapper.mapToDomain(entity)
        }
    }

    override fun observeParkingSessions(): Flow<List<ParkingSession>> {
        return parkingDao.observeParkingSessions().map { entities ->
            entities.map { entity ->
                parkingSessionMapper.mapToDomain(entity)
            }
        }
    }

    private fun generateSessionId(): String {
        return "session_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}
