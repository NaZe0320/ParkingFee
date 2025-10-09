package com.naze.parkingfee.domain.model

/**
 * 주차 구역 도메인 모델 (통일된 주차장/구역 모델)
 */
data class ParkingZone(
    val id: String,
    val name: String,
    val hourlyRate: Double, // 기존 호환성을 위한 필드 (단순 요금 체계용)
    val maxCapacity: Int,
    val currentOccupancy: Int,
    val isActive: Boolean = true,
    val isPublic: Boolean = false, // 공영 주차장 여부
    val isFavorite: Boolean = false, // 즐겨찾기 여부
    
    // 복잡한 요금 체계 (선택사항)
    val feeStructure: FeeStructure? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isAvailable: Boolean
        get() = isActive && currentOccupancy < maxCapacity
    
    val occupancyRate: Double
        get() = if (maxCapacity > 0) currentOccupancy.toDouble() / maxCapacity else 0.0
    
    /**
     * 복잡한 요금 체계를 사용하는지 확인
     */
    val hasComplexFeeStructure: Boolean
        get() = feeStructure != null
    
    /**
     * 표시용 요금 정보 반환 (복잡한 요금 체계가 있으면 그것을, 없으면 hourlyRate 사용)
     */
    fun getDisplayFeeInfo(): String {
        return if (hasComplexFeeStructure) {
            val basic = feeStructure!!.basicFee
            "${basic.fee}원/${basic.durationMinutes}분"
        } else {
            "${hourlyRate.toInt()}원/시간"
        }
    }
}

/**
 * 요금 체계 도메인 모델
 */
data class FeeStructure(
    val basicFee: BasicFeeRule, // 기본 요금 체계
    val additionalFee: AdditionalFeeRule, // 추가 요금 체계
    val dailyMaxFee: DailyMaxFeeRule? = null, // 일 최대 요금 (선택사항)
    val customFeeRules: List<CustomFeeRule> = emptyList() // 커스텀 요금 구간들
)

/**
 * 기본 요금 체계 (처음 N분까지 M원)
 */
data class BasicFeeRule(
    val durationMinutes: Int, // 기본 시간 (분)
    val fee: Int // 기본 요금 (원)
)

/**
 * 추가 요금 체계 (초과 시 매 N분당 M원)
 */
data class AdditionalFeeRule(
    val intervalMinutes: Int, // 추가 요금 간격 (분)
    val fee: Int // 추가 요금 (원)
)

/**
 * 일 최대 요금 체계
 */
data class DailyMaxFeeRule(
    val maxFee: Int // 일 최대 요금 (원)
)

/**
 * 커스텀 요금 구간 (N분 초과 ~ M분 이하, X원)
 */
data class CustomFeeRule(
    val minMinutes: Int, // 최소 시간 (분)
    val maxMinutes: Int?, // 최대 시간 (분), null이면 무제한
    val fee: Int // 요금 (원)
)
