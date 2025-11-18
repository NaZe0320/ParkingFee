package com.naze.parkingfee.domain.repository

import com.naze.parkingfee.domain.model.ParkingAlarm
import kotlinx.coroutines.flow.Flow

/**
 * 알람 관리를 위한 Repository 인터페이스
 */
interface AlarmRepository {
    /**
     * 알람을 추가합니다.
     */
    suspend fun addAlarm(alarm: ParkingAlarm)
    
    /**
     * 알람을 삭제합니다.
     */
    suspend fun deleteAlarm(alarmId: String)
    
    /**
     * 특정 세션의 모든 알람을 조회합니다.
     */
    suspend fun getAlarmsForSession(sessionId: String): List<ParkingAlarm>
    
    /**
     * 특정 세션의 알람을 Flow로 구독합니다.
     */
    fun observeAlarmsForSession(sessionId: String): Flow<List<ParkingAlarm>>
    
    /**
     * 알람의 triggered 상태를 업데이트합니다.
     */
    suspend fun updateAlarmTriggered(alarmId: String, isTriggered: Boolean)
    
    /**
     * 특정 세션의 모든 알람을 삭제합니다.
     */
    suspend fun deleteAlarmsForSession(sessionId: String)
}

