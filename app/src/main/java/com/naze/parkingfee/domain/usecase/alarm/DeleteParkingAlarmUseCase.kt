package com.naze.parkingfee.domain.usecase.alarm

import com.naze.parkingfee.domain.repository.AlarmRepository
import javax.inject.Inject

/**
 * 주차 알람 삭제 UseCase
 */
class DeleteParkingAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    suspend fun execute(alarmId: String) {
        alarmRepository.deleteAlarm(alarmId)
    }
}

