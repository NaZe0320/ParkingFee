package com.naze.parkingfee.domain.model.vehicle

import com.naze.parkingfee.domain.common.discount.DiscountEligibility
import com.naze.parkingfee.domain.common.discount.VehicleDiscountEligibilities

/**
 * 차량 도메인 모델
 */
data class Vehicle(
    val id: String,
    val name: String? = null, // 차량 이름 (선택사항)
    val plateNumber: String? = null, // 번호판 (선택사항) - "12가3456" 형태
    val discountEligibilities: VehicleDiscountEligibilities = VehicleDiscountEligibilities(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 번호판이 입력되었는지 확인
     */
    val hasPlateNumber: Boolean
        get() = !plateNumber.isNullOrBlank()
    
    /**
     * 표시용 번호판 문자열 반환
     */
    val displayPlateNumber: String
        get() = plateNumber ?: "미입력"
    
    /**
     * 표시용 차량 이름 반환
     */
    val displayName: String
        get() = name ?: displayPlateNumber
    
    /**
     * 경차 할인이 적용되는지 확인
     */
    val isCompactCar: Boolean
        get() = discountEligibilities.compactCar.enabled
    
    /**
     * 할인이 적용되는지 확인
     */
    val hasDiscount: Boolean
        get() = discountEligibilities.hasAnyDiscount
}

/**
 * 차량 정보 조회 결과 (국가 API에서 가져온 정보)
 */
data class VehicleInfo(
    val plateNumber: String,
    val vehicleType: String? = null, // 차량 종류
    val manufacturer: String? = null, // 제조사
    val model: String? = null, // 모델명
    val year: Int? = null, // 연식
    val fuelType: String? = null, // 연료 종류
    val displacement: Int? = null, // 배기량
    val isCompactCar: Boolean = false // 경차 여부
)
