package com.naze.parkingfee.domain.model

/**
 * 주차 알람 도메인 모델
 */
data class ParkingAlarm(
    val id: String,
    val sessionId: String,
    val targetAmount: Double,
    val minutesBefore: Int,
    val scheduledTime: Long,
    val isTriggered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

