package com.naze.parkingfee.data.repository

import com.naze.parkingfee.data.datasource.local.dao.ParkingHistoryDao
import com.naze.parkingfee.data.mapper.ParkingHistoryMapper
import com.naze.parkingfee.domain.model.ParkingHistory
import com.naze.parkingfee.domain.repository.ParkingHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 주차 기록 리포지토리 구현체
 */
@Singleton
class ParkingHistoryRepositoryImpl @Inject constructor(
    private val parkingHistoryDao: ParkingHistoryDao
) : ParkingHistoryRepository {
    
    override suspend fun saveParkingHistory(history: ParkingHistory) {
        val entity = ParkingHistoryMapper.toEntity(history)
        parkingHistoryDao.insertParkingHistory(entity)
    }
    
    override fun getAllParkingHistories(): Flow<List<ParkingHistory>> {
        return parkingHistoryDao.getAllParkingHistories()
            .map { entities -> ParkingHistoryMapper.toDomainList(entities) }
    }
    
    override suspend fun getParkingHistoryById(id: String): ParkingHistory? {
        val entity = parkingHistoryDao.getParkingHistoryById(id)
        return entity?.let { ParkingHistoryMapper.toDomain(it) }
    }
    
    override suspend fun deleteParkingHistory(id: String) {
        parkingHistoryDao.deleteParkingHistoryById(id)
    }
    
    override suspend fun deleteAllParkingHistories() {
        parkingHistoryDao.deleteAllParkingHistories()
    }
    
    override fun getParkingHistoriesByZoneId(zoneId: String): Flow<List<ParkingHistory>> {
        return parkingHistoryDao.getParkingHistoriesByZoneId(zoneId)
            .map { entities -> ParkingHistoryMapper.toDomainList(entities) }
    }
}
