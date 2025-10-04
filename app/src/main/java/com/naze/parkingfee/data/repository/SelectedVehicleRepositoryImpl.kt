package com.naze.parkingfee.data.repository

import com.naze.parkingfee.data.datasource.local.datastore.SelectedVehicleDataStore
import com.naze.parkingfee.domain.repository.SelectedVehicleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 선택된 차량 Repository 구현체
 */
@Singleton
class SelectedVehicleRepositoryImpl @Inject constructor(
    private val selectedVehicleDataStore: SelectedVehicleDataStore
) : SelectedVehicleRepository {
    
    override val selectedVehicleId: Flow<String?> = selectedVehicleDataStore.selectedVehicleId
    
    override suspend fun setSelectedVehicleId(vehicleId: String?) {
        selectedVehicleDataStore.setSelectedVehicleId(vehicleId)
    }
    
    override suspend fun clearSelectedVehicleId() {
        selectedVehicleDataStore.clearSelectedVehicleId()
    }
}
