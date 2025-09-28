package com.naze.parkingfee.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 주차장 Room Entity
 */
@Entity(tableName = "parking_lots")
data class ParkingLotEntity(
    @PrimaryKey
    val id: String,
    val name: String? = null,
    val basicFeeDuration: Int,
    val basicFeeAmount: Int,
    val additionalFeeInterval: Int,
    val additionalFeeAmount: Int,
    val dailyMaxFeeEnabled: Boolean = false,
    val dailyMaxFeeAmount: Int? = null,
    val customFeeRulesJson: String? = null, // JSON으로 저장
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
