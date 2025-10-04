package com.naze.parkingfee.domain.model

/**
 * 주차 기록 도메인 모델
 */
data class ParkingHistory(
    val id: String,
    val zoneId: String,
    val zoneNameSnapshot: String, // 삭제된 구역도 표시하기 위한 스냅샷
    val startedAt: Long,
    val endedAt: Long,
    val durationMinutes: Int,
    val feePaid: Double,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 주차 시간을 시:분 형식으로 반환
     */
    fun getFormattedDuration(): String {
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        return String.format("%02d:%02d", hours, minutes)
    }
    
    /**
     * 주차 시작 시간을 날짜 형식으로 반환
     */
    fun getFormattedStartDate(): String {
        val date = java.util.Date(startedAt)
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return formatter.format(date)
    }
    
    /**
     * 주차 종료 시간을 날짜 형식으로 반환
     */
    fun getFormattedEndDate(): String {
        val date = java.util.Date(endedAt)
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return formatter.format(date)
    }
}
