package com.naze.parkingfee.domain.usecase.alarm

import com.naze.parkingfee.domain.repository.AlarmRepository
import javax.inject.Inject

/**
 * 세션의 모든 알람 삭제 UseCase
 */
class DeleteAlarmsForSessionUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    suspend fun execute(sessionId: String) {
        alarmRepository.deleteAlarmsForSession(sessionId)
    }
}

