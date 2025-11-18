package com.naze.parkingfee.utils

import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.domain.model.FeeStructure
import com.naze.parkingfee.domain.model.vehicle.Vehicle

/**
 * 요금 계산 결과
 */
data class FeeResult(
    val original: Double,      // 할인 전 요금
    val discounted: Double    // 할인 후 요금
) {
    val hasDiscount: Boolean
        get() = original != discounted
    
    val discountAmount: Double
        get() = original - discounted
    
    val discountRate: Double
        get() = if (original > 0) discountAmount / original else 0.0
}

/**
 * 주차 요금 계산 유틸리티
 */
object FeeCalculator {
    
    /**
     * 주차 요금을 계산합니다.
     * 
     * @param startTime 주차 시작 시간 (밀리초)
     * @param endTime 주차 종료 시간 (밀리초)
     * @param hourlyRate 시간당 요금
     * @return 계산된 주차 요금
     */
    fun calculateFee(startTime: Long, endTime: Long, hourlyRate: Double): Double {
        val durationMillis = endTime - startTime
        val durationHours = durationMillis.toDouble() / (1000 * 60 * 60)
        
        // 최소 요금 (30분 이하도 30분 요금 적용)
        val minimumHours = 0.5
        val billableHours = maxOf(durationHours, minimumHours)
        
        return billableHours * hourlyRate
    }
    
    /**
     * 현재까지의 주차 요금을 계산합니다.
     * 
     * @param startTime 주차 시작 시간 (밀리초)
     * @param hourlyRate 시간당 요금
     * @return 현재까지의 주차 요금
     */
    fun calculateCurrentFee(startTime: Long, hourlyRate: Double): Double {
        val currentTime = TimeUtils.getCurrentTimestamp()
        return calculateFee(startTime, currentTime, hourlyRate)
    }
    
    /**
     * 주차 구역의 시간당 요금을 반환합니다.
     * 
     * @param zone 주차 구역
     * @return 시간당 요금
     */
    fun getHourlyRate(zone: ParkingZone): Double {
        return zone.hourlyRate
    }
    
    /**
     * 복잡한 요금 체계를 사용하여 주차 요금을 계산합니다.
     * 
     * @param startTime 주차 시작 시간 (밀리초)
     * @param endTime 주차 종료 시간 (밀리초)
     * @param feeStructure 요금 체계
     * @return 계산된 주차 요금
     */
    fun calculateFeeWithStructure(startTime: Long, endTime: Long, feeStructure: FeeStructure): Double {
        val durationMillis = endTime - startTime
        val durationMinutes = durationMillis / (1000 * 60)
        
        // 기본 요금 적용
        var totalFee = feeStructure.basicFee.fee.toDouble()
        
        // 기본 시간을 초과한 경우 추가 요금 계산
        if (durationMinutes > feeStructure.basicFee.durationMinutes) {
            val additionalMinutes = durationMinutes - feeStructure.basicFee.durationMinutes
            val additionalIntervals = (additionalMinutes + feeStructure.additionalFee.intervalMinutes - 1) / feeStructure.additionalFee.intervalMinutes
            val additionalFee = additionalIntervals * feeStructure.additionalFee.fee
            totalFee += additionalFee
        }
        
        // 일 최대 요금 적용
        feeStructure.dailyMaxFee?.let { dailyMax ->
            totalFee = minOf(totalFee, dailyMax.maxFee.toDouble())
        }
        
        return totalFee
    }
    
    /**
     * 주차 구역에 따라 적절한 요금 계산 방법을 선택합니다.
     * 
     * @param startTime 주차 시작 시간 (밀리초)
     * @param endTime 주차 종료 시간 (밀리초)
     * @param zone 주차 구역
     * @return 계산된 주차 요금
     */
    fun calculateFeeForZone(startTime: Long, endTime: Long, zone: ParkingZone): Double {
        return if (zone.hasComplexFeeStructure && zone.feeStructure != null) {
            calculateFeeWithStructure(startTime, endTime, zone.feeStructure)
        } else {
            calculateFee(startTime, endTime, zone.hourlyRate)
        }
    }
    
    /**
     * 현재까지의 주차 요금을 주차 구역에 따라 계산합니다.
     * 
     * @param startTime 주차 시작 시간 (밀리초)
     * @param zone 주차 구역
     * @return 현재까지의 주차 요금
     */
    fun calculateCurrentFeeForZone(startTime: Long, zone: ParkingZone): Double {
        val currentTime = TimeUtils.getCurrentTimestamp()
        return calculateFeeForZone(startTime, currentTime, zone)
    }
    
    /**
     * 주차 구역과 차량에 따라 주차 요금을 계산합니다 (할인 적용).
     * 
     * @param startTime 주차 시작 시간 (밀리초)
     * @param endTime 주차 종료 시간 (밀리초)
     * @param zone 주차 구역
     * @param vehicle 차량 (선택사항)
     * @return 계산된 주차 요금 (할인 적용)
     */
    fun calculateFeeForZone(startTime: Long, endTime: Long, zone: ParkingZone, vehicle: Vehicle?): Double {
        val result = calculateFeeForZoneResult(startTime, endTime, zone, vehicle)
        return result.discounted
    }
    
    /**
     * 주차 구역과 차량에 따라 주차 요금을 계산합니다 (할인 전/후 모두 반환).
     * 
     * @param startTime 주차 시작 시간 (밀리초)
     * @param endTime 주차 종료 시간 (밀리초)
     * @param zone 주차 구역
     * @param vehicle 차량 (선택사항)
     * @return 계산된 주차 요금 결과 (할인 전/후)
     */
    fun calculateFeeForZoneResult(startTime: Long, endTime: Long, zone: ParkingZone, vehicle: Vehicle?): FeeResult {
        // 기본 요금 계산
        val originalFee = calculateFeeForZone(startTime, endTime, zone)
        
        // 할인 적용
        val discountedFee = applyDiscount(originalFee, zone, vehicle)
        
        return FeeResult(
            original = originalFee,
            discounted = discountedFee
        )
    }
    
    /**
     * 현재까지의 주차 요금을 주차 구역과 차량에 따라 계산합니다 (할인 적용).
     * 
     * @param startTime 주차 시작 시간 (밀리초)
     * @param zone 주차 구역
     * @param vehicle 차량 (선택사항)
     * @return 현재까지의 주차 요금 (할인 적용)
     */
    fun calculateCurrentFeeForZone(startTime: Long, zone: ParkingZone, vehicle: Vehicle?): Double {
        val currentTime = TimeUtils.getCurrentTimestamp()
        return calculateFeeForZone(startTime, currentTime, zone, vehicle)
    }
    
    /**
     * 현재까지의 주차 요금을 주차 구역과 차량에 따라 계산합니다 (할인 전/후 모두 반환).
     * 
     * @param startTime 주차 시작 시간 (밀리초)
     * @param zone 주차 구역
     * @param vehicle 차량 (선택사항)
     * @return 현재까지의 주차 요금 결과 (할인 전/후)
     */
    fun calculateCurrentFeeForZoneResult(startTime: Long, zone: ParkingZone, vehicle: Vehicle?): FeeResult {
        val currentTime = TimeUtils.getCurrentTimestamp()
        return calculateFeeForZoneResult(startTime, currentTime, zone, vehicle)
    }
    
    /**
     * 할인을 적용합니다.
     * 
     * @param originalFee 원래 요금
     * @param zone 주차 구역
     * @param vehicle 차량 (선택사항)
     * @return 할인 적용된 요금
     */
    private fun applyDiscount(originalFee: Double, zone: ParkingZone, vehicle: Vehicle?): Double {
        var discountedFee = originalFee
        
        // 공영 주차장에서 경차 할인 적용
        if (zone.isPublic && vehicle?.isCompactCar == true) {
            discountedFee = calculateDiscountedFee(originalFee, 0.5) // 50% 할인
        }
        
        return discountedFee
    }
    
    /**
     * 할인 요금을 계산합니다.
     * 
     * @param originalFee 원래 요금
     * @param discountRate 할인율 (0.0 ~ 1.0)
     * @return 할인된 요금
     */
    fun calculateDiscountedFee(originalFee: Double, discountRate: Double): Double {
        val discountAmount = originalFee * discountRate
        return originalFee - discountAmount
    }
    
    /**
     * 무료 시간을 적용하여 주차 요금을 계산합니다.
     * 
     * @param startTime 주차 시작 시간 (밀리초)
     * @param endTime 주차 종료 시간 (밀리초)
     * @param zone 주차 구역
     * @param freeTimeMinutes 무료 시간 (분)
     * @return 계산된 주차 요금
     */
    fun calculateFeeWithFreeTime(
        startTime: Long,
        endTime: Long,
        zone: ParkingZone,
        freeTimeMinutes: Int
    ): Double {
        val totalDurationMillis = endTime - startTime
        val freeTimeMillis = freeTimeMinutes * 60 * 1000L
        
        // 무료 시간이 총 주차 시간보다 길면 0원
        if (freeTimeMillis >= totalDurationMillis) {
            return 0.0
        }
        
        // 무료 시간을 뺀 실제 과금 시간 계산
        val billableDurationMillis = totalDurationMillis - freeTimeMillis
        val adjustedEndTime = startTime + billableDurationMillis
        
        return calculateFeeForZone(startTime, adjustedEndTime, zone)
    }
    
    /**
     * 무료 시간과 차량 할인을 모두 적용하여 주차 요금을 계산합니다.
     * 
     * @param startTime 주차 시작 시간 (밀리초)
     * @param endTime 주차 종료 시간 (밀리초)
     * @param zone 주차 구역
     * @param vehicle 차량 (선택사항)
     * @param freeTimeMinutes 무료 시간 (분)
     * @return 계산된 주차 요금 (할인 적용)
     */
    fun calculateFeeWithFreeTimeAndVehicle(
        startTime: Long,
        endTime: Long,
        zone: ParkingZone,
        vehicle: Vehicle?,
        freeTimeMinutes: Int
    ): Double {
        val totalDurationMillis = endTime - startTime
        val freeTimeMillis = freeTimeMinutes * 60 * 1000L
        
        // 무료 시간이 총 주차 시간보다 길면 0원
        if (freeTimeMillis >= totalDurationMillis) {
            return 0.0
        }
        
        // 무료 시간을 뺀 실제 과금 시간 계산
        val billableDurationMillis = totalDurationMillis - freeTimeMillis
        val adjustedEndTime = startTime + billableDurationMillis
        
        // 차량 할인 적용
        return calculateFeeForZone(startTime, adjustedEndTime, zone, vehicle)
    }
    
    /**
     * 무료 시간과 차량 할인을 모두 적용하여 주차 요금을 계산합니다 (할인 전/후 모두 반환).
     * 
     * @param startTime 주차 시작 시간 (밀리초)
     * @param endTime 주차 종료 시간 (밀리초)
     * @param zone 주차 구역
     * @param vehicle 차량 (선택사항)
     * @param freeTimeMinutes 무료 시간 (분)
     * @return 계산된 주차 요금 결과 (할인 전/후)
     */
    fun calculateFeeWithFreeTimeAndVehicleResult(
        startTime: Long,
        endTime: Long,
        zone: ParkingZone,
        vehicle: Vehicle?,
        freeTimeMinutes: Int
    ): FeeResult {
        val totalDurationMillis = endTime - startTime
        val freeTimeMillis = freeTimeMinutes * 60 * 1000L
        
        // 무료 시간이 총 주차 시간보다 길면 0원
        if (freeTimeMillis >= totalDurationMillis) {
            return FeeResult(original = 0.0, discounted = 0.0)
        }
        
        // 무료 시간을 뺀 실제 과금 시간 계산
        val billableDurationMillis = totalDurationMillis - freeTimeMillis
        val adjustedEndTime = startTime + billableDurationMillis
        
        // 차량 할인 적용
        return calculateFeeForZoneResult(startTime, adjustedEndTime, zone, vehicle)
    }
}
