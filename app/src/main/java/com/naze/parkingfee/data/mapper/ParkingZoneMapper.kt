package com.naze.parkingfee.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.domain.model.FeeStructure
import com.naze.parkingfee.domain.model.BasicFeeRule
import com.naze.parkingfee.domain.model.AdditionalFeeRule
import com.naze.parkingfee.domain.model.DailyMaxFeeRule
import com.naze.parkingfee.domain.model.CustomFeeRule
import com.naze.parkingfee.data.datasource.local.entity.ParkingZoneEntity
import javax.inject.Inject

/**
 * ParkingZone Entity와 Domain Model 간의 매핑
 */
class ParkingZoneMapper @Inject constructor(
    private val gson: Gson
) {

    private val customFeeRulesType = object : TypeToken<List<CustomFeeRule>>() {}.type
    
    fun mapToDomain(entity: ParkingZoneEntity): ParkingZone {
        val feeStructure = if (entity.basicFeeDuration != null && entity.basicFeeAmount != null) {
            FeeStructure(
                basicFee = BasicFeeRule(
                    durationMinutes = entity.basicFeeDuration,
                    fee = entity.basicFeeAmount
                ),
                additionalFee = AdditionalFeeRule(
                    intervalMinutes = entity.additionalFeeInterval ?: 10,
                    fee = entity.additionalFeeAmount ?: 500
                ),
                dailyMaxFee = if (entity.dailyMaxFeeEnabled && entity.dailyMaxFeeAmount != null) {
                    DailyMaxFeeRule(maxFee = entity.dailyMaxFeeAmount)
                } else null,
                customFeeRules = parseCustomFeeRules(entity.customFeeRulesJson)
            )
        } else null
        
        return ParkingZone(
            id = entity.id,
            name = entity.name,
            hourlyRate = entity.hourlyRate,
            maxCapacity = entity.maxCapacity,
            currentOccupancy = entity.currentOccupancy,
            isActive = entity.isActive,
            isPublic = entity.isPublic,
            isFavorite = entity.isFavorite,
            feeStructure = feeStructure,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
    
    fun mapToEntity(domain: ParkingZone): ParkingZoneEntity {
        return ParkingZoneEntity(
            id = domain.id,
            name = domain.name,
            hourlyRate = domain.hourlyRate,
            maxCapacity = domain.maxCapacity,
            currentOccupancy = domain.currentOccupancy,
            isActive = domain.isActive,
            isPublic = domain.isPublic,
            isFavorite = domain.isFavorite,
            basicFeeDuration = domain.feeStructure?.basicFee?.durationMinutes,
            basicFeeAmount = domain.feeStructure?.basicFee?.fee,
            additionalFeeInterval = domain.feeStructure?.additionalFee?.intervalMinutes,
            additionalFeeAmount = domain.feeStructure?.additionalFee?.fee,
            dailyMaxFeeEnabled = domain.feeStructure?.dailyMaxFee != null,
            dailyMaxFeeAmount = domain.feeStructure?.dailyMaxFee?.maxFee,
            customFeeRulesJson = serializeCustomFeeRules(domain.feeStructure?.customFeeRules),
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
    
    private fun parseCustomFeeRules(json: String?): List<CustomFeeRule> {
        if (json.isNullOrBlank()) {
            return emptyList()
        }

        return runCatching {
            gson.fromJson<List<CustomFeeRule>>(json, customFeeRulesType)
        }.getOrElse { emptyList() } ?: emptyList()
    }
    
    private fun serializeCustomFeeRules(rules: List<CustomFeeRule>?): String? {
        if (rules.isNullOrEmpty()) {
            return null
        }

        return runCatching {
            gson.toJson(rules, customFeeRulesType)
        }.getOrNull()
    }
}
