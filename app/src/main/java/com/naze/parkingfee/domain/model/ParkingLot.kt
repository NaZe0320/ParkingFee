package com.naze.parkingfee.domain.model

/**
 * 주차장 도메인 모델
 */
data class ParkingLot(
    val id: String,
    val name: String? = null, // null이면 "주차장1", "주차장2" 등으로 표시
    val feeStructure: FeeStructure,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 주차장 표시 이름을 반환합니다.
     * name이 null이면 "주차장{순번}" 형태로 반환합니다.
     */
    fun getDisplayName(sequenceNumber: Int): String {
        return name ?: "주차장$sequenceNumber"
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
