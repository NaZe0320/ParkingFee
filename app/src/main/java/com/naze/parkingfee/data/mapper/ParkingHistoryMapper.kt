package com.naze.parkingfee.data.mapper

import com.naze.parkingfee.data.datasource.local.entity.ParkingHistoryEntity
import com.naze.parkingfee.domain.model.ParkingHistory

/**
 * 주차 기록 매퍼
 */
object ParkingHistoryMapper {
    
    /**
     * Entity를 Domain Model로 변환
     */
    fun toDomain(entity: ParkingHistoryEntity): ParkingHistory {
        return ParkingHistory(
            id = entity.id,
            zoneId = entity.zoneId,
            zoneNameSnapshot = entity.zoneNameSnapshot,
            startedAt = entity.startedAt,
            endedAt = entity.endedAt,
            durationMinutes = entity.durationMinutes,
            feePaid = entity.feePaid,
            createdAt = entity.createdAt
        )
    }
    
    /**
     * Domain Model을 Entity로 변환
     */
    fun toEntity(domain: ParkingHistory): ParkingHistoryEntity {
        return ParkingHistoryEntity(
            id = domain.id,
            zoneId = domain.zoneId,
            zoneNameSnapshot = domain.zoneNameSnapshot,
            startedAt = domain.startedAt,
            endedAt = domain.endedAt,
            durationMinutes = domain.durationMinutes,
            feePaid = domain.feePaid,
            createdAt = domain.createdAt
        )
    }
    
    /**
     * Entity 리스트를 Domain Model 리스트로 변환
     */
    fun toDomainList(entities: List<ParkingHistoryEntity>): List<ParkingHistory> {
        return entities.map { toDomain(it) }
    }
}
