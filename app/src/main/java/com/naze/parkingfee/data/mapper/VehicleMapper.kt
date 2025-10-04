package com.naze.parkingfee.data.mapper

import com.naze.parkingfee.data.datasource.local.entity.VehicleEntity
import com.naze.parkingfee.domain.model.vehicle.Vehicle
import com.naze.parkingfee.domain.common.discount.DiscountEligibility
import com.naze.parkingfee.domain.common.discount.VehicleDiscountEligibilities

/**
 * 차량 데이터 매퍼
 */
object VehicleMapper {
    
    /**
     * Entity를 Domain 모델로 변환
     */
    fun toDomain(entity: VehicleEntity): Vehicle {
        return Vehicle(
            id = entity.id,
            name = entity.name,
            plateNumber = entity.plateNumber,
            discountEligibilities = VehicleDiscountEligibilities(
                compactCar = DiscountEligibility.CompactCar(entity.isCompactCar),
                nationalMerit = DiscountEligibility.NationalMerit(entity.isNationalMerit),
                disabled = DiscountEligibility.Disabled(entity.isDisabled)
            ),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
    
    /**
     * Domain 모델을 Entity로 변환
     */
    fun toEntity(domain: Vehicle): VehicleEntity {
        return VehicleEntity(
            id = domain.id,
            name = domain.name,
            plateNumber = domain.plateNumber,
            isCompactCar = domain.discountEligibilities.compactCar.enabled,
            isNationalMerit = domain.discountEligibilities.nationalMerit.enabled,
            isDisabled = domain.discountEligibilities.disabled.enabled,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
    
    /**
     * Entity 리스트를 Domain 모델 리스트로 변환
     */
    fun toDomainList(entities: List<VehicleEntity>): List<Vehicle> {
        return entities.map { toDomain(it) }
    }
    
    /**
     * Domain 모델 리스트를 Entity 리스트로 변환
     */
    fun toEntityList(domains: List<Vehicle>): List<VehicleEntity> {
        return domains.map { toEntity(it) }
    }
}
