package com.naze.parkingfee.data.mapper

import com.naze.parkingfee.domain.model.ParkingSession
import com.naze.parkingfee.data.datasource.local.entity.ParkingSessionEntity
import javax.inject.Inject

/**
 * ParkingSession Entity와 Domain Model 간의 매핑
 */
class ParkingSessionMapper @Inject constructor() {
    
    fun mapToDomain(entity: ParkingSessionEntity): ParkingSession {
        return ParkingSession(
            id = entity.id,
            zoneId = entity.zoneId,
            startTime = entity.startTime,
            endTime = entity.endTime,
            totalFee = entity.totalFee,
            isActive = entity.isActive
        )
    }
    
    fun mapToEntity(domain: ParkingSession): ParkingSessionEntity {
        return ParkingSessionEntity(
            id = domain.id,
            zoneId = domain.zoneId,
            startTime = domain.startTime,
            endTime = domain.endTime,
            totalFee = domain.totalFee,
            isActive = domain.isActive
        )
    }
}
