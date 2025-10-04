package com.naze.parkingfee.domain.common.discount

/**
 * 차량 할인 자격 도메인 모델
 * 확장 가능한 구조로 설계 (경차, 국가유공자 등)
 */
sealed class DiscountEligibility {
    
    /**
     * 경차 할인 자격
     */
    data class CompactCar(val enabled: Boolean) : DiscountEligibility()
    
    /**
     * 국가유공자 할인 자격 (향후 확장)
     */
    data class NationalMerit(val enabled: Boolean) : DiscountEligibility()
    
    /**
     * 장애인 할인 자격 (향후 확장)
     */
    data class Disabled(val enabled: Boolean) : DiscountEligibility()
    
    /**
     * 할인 자격이 활성화되어 있는지 확인
     */
    val isEnabled: Boolean
        get() = when (this) {
            is CompactCar -> enabled
            is NationalMerit -> enabled
            is Disabled -> enabled
        }
    
    /**
     * 할인 자격 타입 이름 반환
     */
    val typeName: String
        get() = when (this) {
            is CompactCar -> "경차"
            is NationalMerit -> "국가유공자"
            is Disabled -> "장애인"
        }
}

/**
 * 차량의 모든 할인 자격을 관리하는 데이터 클래스
 */
data class VehicleDiscountEligibilities(
    val compactCar: DiscountEligibility.CompactCar = DiscountEligibility.CompactCar(false),
    val nationalMerit: DiscountEligibility.NationalMerit = DiscountEligibility.NationalMerit(false),
    val disabled: DiscountEligibility.Disabled = DiscountEligibility.Disabled(false)
) {
    /**
     * 활성화된 할인 자격 목록 반환
     */
    val activeEligibilities: List<DiscountEligibility>
        get() = listOf(compactCar, nationalMerit, disabled).filter { it.isEnabled }
    
    /**
     * 할인이 적용되는지 확인
     */
    val hasAnyDiscount: Boolean
        get() = activeEligibilities.isNotEmpty()
}
