package com.naze.parkingfee.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 주차 구역 Room Entity (통일된 주차장/구역 모델)
 */
@Entity(tableName = "parking_zones")
data class ParkingZoneEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val hourlyRate: Double, // 기존 호환성을 위한 필드
    val maxCapacity: Int,
    val currentOccupancy: Int,
    val isActive: Boolean = true,
    
    // 복잡한 요금 체계 필드들 (선택사항)
    val basicFeeDuration: Int? = null,
    val basicFeeAmount: Int? = null,
    val additionalFeeInterval: Int? = null,
    val additionalFeeAmount: Int? = null,
    val dailyMaxFeeEnabled: Boolean = false,
    val dailyMaxFeeAmount: Int? = null,
    val customFeeRulesJson: String? = null, // JSON으로 저장
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
