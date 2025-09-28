package com.naze.parkingfee.data.mapper

import com.naze.parkingfee.data.datasource.local.entity.ParkingLotEntity
import com.naze.parkingfee.domain.model.ParkingLot
import com.naze.parkingfee.domain.model.FeeStructure
import com.naze.parkingfee.domain.model.BasicFeeRule
import com.naze.parkingfee.domain.model.AdditionalFeeRule
import com.naze.parkingfee.domain.model.DailyMaxFeeRule
import com.naze.parkingfee.domain.model.CustomFeeRule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

/**
 * 주차장 데이터 매퍼
 */
class ParkingLotMapper @Inject constructor(
    private val gson: Gson
) {
    
    fun mapToDomain(entity: ParkingLotEntity): ParkingLot {
        return ParkingLot(
            id = entity.id,
            name = entity.name,
            feeStructure = FeeStructure(
                basicFee = BasicFeeRule(
                    durationMinutes = entity.basicFeeDuration,
                    fee = entity.basicFeeAmount
                ),
                additionalFee = AdditionalFeeRule(
                    intervalMinutes = entity.additionalFeeInterval,
                    fee = entity.additionalFeeAmount
                ),
                dailyMaxFee = if (entity.dailyMaxFeeEnabled && entity.dailyMaxFeeAmount != null) {
                    DailyMaxFeeRule(maxFee = entity.dailyMaxFeeAmount)
                } else null,
                customFeeRules = parseCustomFeeRules(entity.customFeeRulesJson)
            ),
            isActive = entity.isActive,
            createdAt = entity.createdAt
        )
    }
    
    fun mapToEntity(domain: ParkingLot): ParkingLotEntity {
        return ParkingLotEntity(
            id = domain.id,
            name = domain.name,
            basicFeeDuration = domain.feeStructure.basicFee.durationMinutes,
            basicFeeAmount = domain.feeStructure.basicFee.fee,
            additionalFeeInterval = domain.feeStructure.additionalFee.intervalMinutes,
            additionalFeeAmount = domain.feeStructure.additionalFee.fee,
            dailyMaxFeeEnabled = domain.feeStructure.dailyMaxFee != null,
            dailyMaxFeeAmount = domain.feeStructure.dailyMaxFee?.maxFee,
            customFeeRulesJson = serializeCustomFeeRules(domain.feeStructure.customFeeRules),
            isActive = domain.isActive,
            createdAt = domain.createdAt
        )
    }
    
    private fun parseCustomFeeRules(json: String?): List<CustomFeeRule> {
        if (json.isNullOrBlank()) return emptyList()
        
        return try {
            val type = object : TypeToken<List<CustomFeeRule>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun serializeCustomFeeRules(rules: List<CustomFeeRule>): String? {
        if (rules.isEmpty()) return null
        
        return try {
            gson.toJson(rules)
        } catch (e: Exception) {
            null
        }
    }
}
