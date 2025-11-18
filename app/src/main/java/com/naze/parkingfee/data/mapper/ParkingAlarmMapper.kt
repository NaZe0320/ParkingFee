package com.naze.parkingfee.data.mapper

import com.naze.parkingfee.data.datasource.local.entity.ParkingAlarmEntity
import com.naze.parkingfee.domain.model.ParkingAlarm

/**
 * ParkingAlarm 매퍼
 */
object ParkingAlarmMapper {
    
    /**
     * Entity를 Domain Model로 변환
     */
    fun toDomain(entity: ParkingAlarmEntity): ParkingAlarm {
        return ParkingAlarm(
            id = entity.id,
            sessionId = entity.sessionId,
            targetAmount = entity.targetAmount,
            minutesBefore = entity.minutesBefore,
            scheduledTime = entity.scheduledTime,
            isTriggered = entity.isTriggered,
            createdAt = entity.createdAt
        )
    }
    
    /**
     * Domain Model을 Entity로 변환
     */
    fun toEntity(domain: ParkingAlarm): ParkingAlarmEntity {
        return ParkingAlarmEntity(
            id = domain.id,
            sessionId = domain.sessionId,
            targetAmount = domain.targetAmount,
            minutesBefore = domain.minutesBefore,
            scheduledTime = domain.scheduledTime,
            isTriggered = domain.isTriggered,
            createdAt = domain.createdAt
        )
    }
    
    /**
     * Entity 리스트를 Domain Model 리스트로 변환
     */
    fun toDomainList(entities: List<ParkingAlarmEntity>): List<ParkingAlarm> {
        return entities.map { toDomain(it) }
    }
}

