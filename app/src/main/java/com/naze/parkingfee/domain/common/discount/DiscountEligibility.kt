package com.naze.parkingfee.domain.common.discount

/**
 * 차량 할인 자격 도메인 모델
 * 확장 가능한 구조로 설계 (경차, 저공해 차 등)
 */
sealed class DiscountEligibility {
    
    /**
     * 경차 할인 자격
     */
    data class CompactCar(val enabled: Boolean) : DiscountEligibility()
    
    /**
     * 저공해 차 할인 자격
     */
    data class LowEmission(val enabled: Boolean) : DiscountEligibility()
    
    /**
     * 할인 자격이 활성화되어 있는지 확인
     */
    val isEnabled: Boolean
        get() = when (this) {
            is CompactCar -> enabled
            is LowEmission -> enabled
        }
    
    /**
     * 할인 자격 타입 이름 반환
     */
    val typeName: String
        get() = when (this) {
            is CompactCar -> "경차"
            is LowEmission -> "저공해 차"
        }
}

/**
 * 차량의 모든 할인 자격을 관리하는 데이터 클래스
 */
data class VehicleDiscountEligibilities(
    val compactCar: DiscountEligibility.CompactCar = DiscountEligibility.CompactCar(false),
    val lowEmission: DiscountEligibility.LowEmission = DiscountEligibility.LowEmission(false)
) {
    /**
     * 활성화된 할인 자격 목록 반환
     */
    val activeEligibilities: List<DiscountEligibility>
        get() = listOf(compactCar, lowEmission).filter { it.isEnabled }
    
    /**
     * 할인이 적용되는지 확인
     */
    val hasAnyDiscount: Boolean
        get() = activeEligibilities.isNotEmpty()
}
