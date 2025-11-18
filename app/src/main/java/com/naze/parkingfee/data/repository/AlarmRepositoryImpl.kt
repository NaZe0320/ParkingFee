package com.naze.parkingfee.data.repository

import com.naze.parkingfee.data.datasource.local.dao.ParkingAlarmDao
import com.naze.parkingfee.data.mapper.ParkingAlarmMapper
import com.naze.parkingfee.domain.model.ParkingAlarm
import com.naze.parkingfee.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 알람 Repository 구현체
 */
class AlarmRepositoryImpl @Inject constructor(
    private val alarmDao: ParkingAlarmDao
) : AlarmRepository {
    
    override suspend fun addAlarm(alarm: ParkingAlarm) {
        alarmDao.insert(ParkingAlarmMapper.toEntity(alarm))
    }
    
    override suspend fun deleteAlarm(alarmId: String) {
        alarmDao.deleteById(alarmId)
    }
    
    override suspend fun getAlarmsForSession(sessionId: String): List<ParkingAlarm> {
        return alarmDao.getBySessionId(sessionId).map { 
            ParkingAlarmMapper.toDomain(it)
        }
    }
    
    override fun observeAlarmsForSession(sessionId: String): Flow<List<ParkingAlarm>> {
        return alarmDao.observeBySessionId(sessionId).map { entities ->
            ParkingAlarmMapper.toDomainList(entities)
        }
    }
    
    override suspend fun updateAlarmTriggered(alarmId: String, isTriggered: Boolean) {
        alarmDao.updateTriggered(alarmId, isTriggered)
    }
    
    override suspend fun deleteAlarmsForSession(sessionId: String) {
        alarmDao.deleteBySessionId(sessionId)
    }
}

