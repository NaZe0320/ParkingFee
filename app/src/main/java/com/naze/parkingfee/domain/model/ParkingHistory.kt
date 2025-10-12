package com.naze.parkingfee.domain.model

import java.util.Locale

/**
 * 주차 기록 도메인 모델
 */
data class ParkingHistory(
    val id: String,
    val zoneId: String,
    val zoneNameSnapshot: String, // 삭제된 구역도 표시하기 위한 스냅샷
    val vehicleId: String? = null,
    val vehicleNameSnapshot: String? = null,
    val vehiclePlateSnapshot: String? = null,
    val startedAt: Long,
    val endedAt: Long,
    val durationMinutes: Int,
    val feePaid: Double,
    val originalFee: Double? = null, // 할인 전 원래 요금
    val hasDiscount: Boolean = false, // 할인 적용 여부
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 주차 시간을 시:분 형식으로 반환
     */
    fun getFormattedDuration(): String {
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        return String.format(Locale.getDefault(),"%02d:%02d", hours, minutes)
    }
    
    /**
     * 주차 시작 시간을 날짜 형식으로 반환
     */
    fun getFormattedDate(): String {
        val date = java.util.Date(startedAt)
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return formatter.format(date)
    }
}
