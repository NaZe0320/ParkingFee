package com.naze.parkingfee.data.mapper

import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.data.datasource.local.entity.ParkingZoneEntity
import javax.inject.Inject

/**
 * ParkingZone Entity와 Domain Model 간의 매핑
 */
class ParkingZoneMapper @Inject constructor() {
    
    fun mapToDomain(entity: ParkingZoneEntity): ParkingZone {
        return ParkingZone(
            id = entity.id,
            name = entity.name,
            hourlyRate = entity.hourlyRate,
            maxCapacity = entity.maxCapacity,
            currentOccupancy = entity.currentOccupancy,
            isActive = entity.isActive
        )
    }
    
    fun mapToEntity(domain: ParkingZone): ParkingZoneEntity {
        return ParkingZoneEntity(
            id = domain.id,
            name = domain.name,
            hourlyRate = domain.hourlyRate,
            maxCapacity = domain.maxCapacity,
            currentOccupancy = domain.currentOccupancy,
            isActive = domain.isActive
        )
    }
}
