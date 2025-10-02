package com.naze.parkingfee.utils

import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.domain.model.FeeStructure

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
}
