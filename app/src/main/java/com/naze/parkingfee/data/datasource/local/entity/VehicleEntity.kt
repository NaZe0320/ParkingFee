package com.naze.parkingfee.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 차량 Room Entity
 */
@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey
    val id: String,
    val name: String? = null, // 차량 이름 (선택사항)
    val plateNumber: String? = null, // 번호판 (선택사항) - "12가3456" 형태
    val isCompactCar: Boolean = false, // 경차 여부
    val isLowEmission: Boolean = false, // 저공해 차 여부
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
