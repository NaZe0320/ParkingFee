package com.naze.parkingfee.domain.usecase.alarm

import com.naze.parkingfee.domain.model.ParkingAlarm
import com.naze.parkingfee.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 주차 알람 조회 UseCase
 */
class GetParkingAlarmsUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    suspend fun execute(sessionId: String): List<ParkingAlarm> {
        return alarmRepository.getAlarmsForSession(sessionId)
    }
    
    fun observe(sessionId: String): Flow<List<ParkingAlarm>> {
        return alarmRepository.observeAlarmsForSession(sessionId)
    }
}

