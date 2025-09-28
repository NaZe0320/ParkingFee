package com.naze.parkingfee.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 주차 구역 Room Entity
 */
@Entity(tableName = "parking_zones")
data class ParkingZoneEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val hourlyRate: Double,
    val maxCapacity: Int,
    val currentOccupancy: Int,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
