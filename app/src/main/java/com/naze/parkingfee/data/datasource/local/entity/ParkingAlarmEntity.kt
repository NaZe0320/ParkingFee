package com.naze.parkingfee.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 주차 알람 Room Entity
 */
@Entity(tableName = "parking_alarms")
data class ParkingAlarmEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val targetAmount: Double,
    val minutesBefore: Int,
    val scheduledTime: Long,
    val isTriggered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

