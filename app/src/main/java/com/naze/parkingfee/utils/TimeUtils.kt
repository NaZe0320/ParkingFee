package com.naze.parkingfee.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * 시간 관련 유틸리티 함수들
 */
object TimeUtils {
    
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    
    /**
     * 타임스탬프를 시간 형식으로 변환
     */
    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }
    
    /**
     * 타임스탬프를 날짜 시간 형식으로 변환
     */
    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }
    
    /**
     * 경과 시간을 HH:mm 형식으로 변환
     */
    fun formatDuration(durationMillis: Long): String {
        val hours = durationMillis / (1000 * 60 * 60)
        val minutes = (durationMillis % (1000 * 60 * 60)) / (1000 * 60)
        
        return String.format("%02d:%02d", hours, minutes)
    }
    
    /**
     * 현재 시간의 타임스탬프 반환
     */
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }
    
    /**
     * 두 시간 사이의 차이를 밀리초로 반환
     */
    fun getDurationMillis(startTime: Long, endTime: Long): Long {
        return endTime - startTime
    }
}
