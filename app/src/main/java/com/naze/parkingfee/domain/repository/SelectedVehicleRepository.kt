package com.naze.parkingfee.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * 선택된 차량 관리 Repository 인터페이스
 */
interface SelectedVehicleRepository {
    
    /**
     * 선택된 차량 ID를 관찰합니다.
     */
    val selectedVehicleId: Flow<String?>
    
    /**
     * 선택된 차량 ID를 설정합니다.
     */
    suspend fun setSelectedVehicleId(vehicleId: String?)
    
    /**
     * 선택된 차량 ID를 제거합니다.
     */
    suspend fun clearSelectedVehicleId()
}
