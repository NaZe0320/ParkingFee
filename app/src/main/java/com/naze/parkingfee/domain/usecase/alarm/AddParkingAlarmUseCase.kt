package com.naze.parkingfee.domain.usecase.alarm

import com.naze.parkingfee.domain.model.ParkingAlarm
import com.naze.parkingfee.domain.repository.AlarmRepository
import javax.inject.Inject

/**
 * 주차 알람 추가 UseCase
 */
class AddParkingAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    suspend fun execute(alarm: ParkingAlarm) {
        alarmRepository.addAlarm(alarm)
    }
}

