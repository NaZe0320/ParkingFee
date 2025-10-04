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
    val startedAt: Long,
    val endedAt: Long,
    val durationMinutes: Int,
    val feePaid: Double,
    val createdAt: Long
)
