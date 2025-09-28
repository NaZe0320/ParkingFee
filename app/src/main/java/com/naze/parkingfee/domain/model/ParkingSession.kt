package com.naze.parkingfee.domain.model

/**
 * 주차 세션 도메인 모델
 */
data class ParkingSession(
    val id: String,
    val zoneId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val totalFee: Double = 0.0,
    val isActive: Boolean = true
) {
    val duration: Long
        get() = (endTime ?: System.currentTimeMillis()) - startTime
    
    val durationInMinutes: Long
        get() = duration / (1000 * 60)
    
    val durationInHours: Double
        get() = durationInMinutes.toDouble() / 60.0
}
