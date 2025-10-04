package com.naze.parkingfee.data.datasource.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 선택된 차량 ID를 관리하는 DataStore
 */
@Singleton
class SelectedVehicleDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "selected_vehicle")
        private val SELECTED_VEHICLE_ID_KEY = stringPreferencesKey("selected_vehicle_id")
    }
    
    /**
     * 선택된 차량 ID를 가져옵니다.
     */
    val selectedVehicleId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_VEHICLE_ID_KEY]
    }
    
    /**
     * 선택된 차량 ID를 설정합니다.
     */
    suspend fun setSelectedVehicleId(vehicleId: String?) {
        context.dataStore.edit { preferences ->
            if (vehicleId != null) {
                preferences[SELECTED_VEHICLE_ID_KEY] = vehicleId
            } else {
                preferences.remove(SELECTED_VEHICLE_ID_KEY)
            }
        }
    }
    
    /**
     * 선택된 차량 ID를 제거합니다.
     */
    suspend fun clearSelectedVehicleId() {
        context.dataStore.edit { preferences ->
            preferences.remove(SELECTED_VEHICLE_ID_KEY)
        }
    }
}
