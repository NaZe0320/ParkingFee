package com.naze.parkingfee.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 주차 세션 Room Entity
 */
@Entity(tableName = "parking_sessions")
data class ParkingSessionEntity(
    @PrimaryKey
    val id: String,
    val zoneId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val totalFee: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
