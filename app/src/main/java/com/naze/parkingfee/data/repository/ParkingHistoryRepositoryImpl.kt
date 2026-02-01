package com.naze.parkingfee.data.repository

import com.naze.parkingfee.data.datasource.local.dao.ParkingHistoryDao
import com.naze.parkingfee.data.datasource.remote.firestore.ParkingHistoryRemoteDataSource
import com.naze.parkingfee.data.mapper.ParkingHistoryMapper
import com.naze.parkingfee.domain.model.ParkingHistory
import com.naze.parkingfee.domain.repository.ParkingHistoryRepository
import com.naze.parkingfee.utils.SyncLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 주차 기록 리포지토리 구현체
 */
@Singleton
class ParkingHistoryRepositoryImpl @Inject constructor(
    private val parkingHistoryDao: ParkingHistoryDao,
    private val parkingHistoryRemoteDataSource: ParkingHistoryRemoteDataSource
) : ParkingHistoryRepository {
    
    override suspend fun saveParkingHistory(history: ParkingHistory) {
        val entity = ParkingHistoryMapper.toEntity(history)
        parkingHistoryDao.insertParkingHistory(entity)

        // Firestore는 로깅/기록용. 실패해도 로컬 저장은 유지한다.
        val remoteResult = parkingHistoryRemoteDataSource.upsertParkingHistory(history)
        if (remoteResult.isSuccess) {
            SyncLogger.logSyncAttempt(entityType = "ParkingHistoryFirestoreUpsert", entityId = history.id, uid = null)
        } else {
            SyncLogger.logSyncSkipped(
                entityType = "ParkingHistoryFirestoreUpsert",
                entityId = history.id,
                reason = remoteResult.exceptionOrNull()?.message ?: "remote upsert failed"
            )
        }
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

        // Firestore는 로깅/기록용. 실패해도 로컬 삭제는 유지한다.
        val remoteResult = parkingHistoryRemoteDataSource.deleteParkingHistory(id)
        if (remoteResult.isSuccess) {
            SyncLogger.logSyncAttempt(entityType = "ParkingHistoryFirestoreDelete", entityId = id, uid = null)
        } else {
            SyncLogger.logSyncSkipped(
                entityType = "ParkingHistoryFirestoreDelete",
                entityId = id,
                reason = remoteResult.exceptionOrNull()?.message ?: "remote delete failed"
            )
        }
    }
}
