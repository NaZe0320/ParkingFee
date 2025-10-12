package com.naze.parkingfee.domain.repository

import com.naze.parkingfee.domain.model.ParkingHistory
import kotlinx.coroutines.flow.Flow

/**
 * 주차 기록 리포지토리 인터페이스
 */
interface ParkingHistoryRepository {
    /**
     * 주차 기록 저장
     */
    suspend fun saveParkingHistory(history: ParkingHistory)
    
    /**
     * 모든 주차 기록 조회 (최신순)
     */
    fun getAllParkingHistories(): Flow<List<ParkingHistory>>
    
    /**
     * 특정 주차 기록 조회
     */
    suspend fun getParkingHistoryById(id: String): ParkingHistory?
    
    /**
     * 특정 주차 기록 삭제
     */
    suspend fun deleteParkingHistory(id: String)
}
