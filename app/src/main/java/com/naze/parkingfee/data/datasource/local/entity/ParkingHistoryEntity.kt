package com.naze.parkingfee.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 주차 기록 Room 엔티티
 */
@Entity(tableName = "parking_history")
data class ParkingHistoryEntity(
    @PrimaryKey
    val id: String,
    val zoneId: String,
    val zoneNameSnapshot: String,
    val vehicleId: String? = null,
    val vehicleNameSnapshot: String? = null,
    val vehiclePlateSnapshot: String? = null,
    val startedAt: Long,
    val endedAt: Long,
    val durationMinutes: Int,
    val feePaid: Double,
    val originalFee: Double? = null, // 할인 전 원래 요금
    val hasDiscount: Boolean = false, // 할인 적용 여부
    val createdAt: Long
)
