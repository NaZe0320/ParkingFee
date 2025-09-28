package com.naze.parkingfee.domain.model

/**
 * 주차 구역 도메인 모델
 */
data class ParkingZone(
    val id: String,
    val name: String,
    val hourlyRate: Double,
    val maxCapacity: Int,
    val currentOccupancy: Int,
    val isActive: Boolean = true
) {
    val isAvailable: Boolean
        get() = isActive && currentOccupancy < maxCapacity
    
    val occupancyRate: Double
        get() = if (maxCapacity > 0) currentOccupancy.toDouble() / maxCapacity else 0.0
}
