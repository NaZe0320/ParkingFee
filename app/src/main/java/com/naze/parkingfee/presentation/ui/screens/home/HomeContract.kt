package com.naze.parkingfee.presentation.ui.screens.home

import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.domain.model.ParkingSession
import com.naze.parkingfee.presentation.ui.screens.home.components.ZoneAction

/**
 * 홈 화면의 MVI Contract
 * Intent, State, Effect를 정의합니다.
 */
object HomeContract {

    /**
     * 사용자 액션을 나타내는 Intent
     */
    sealed class HomeIntent {
        data class StartParking(val zoneId: String) : HomeIntent()
        data class StopParking(val sessionId: String) : HomeIntent()
        object RefreshParkingInfo : HomeIntent()
        object NavigateToSettings : HomeIntent()
        object NavigateToHistory : HomeIntent()
        object NavigateToAddParkingLot : HomeIntent()
        data class SelectZone(val zone: ParkingZone) : HomeIntent()
        data class SelectVehicle(val vehicle: com.naze.parkingfee.domain.model.vehicle.Vehicle) : HomeIntent()
        data class RequestZoneAction(val zone: ParkingZone, val action: ZoneAction) : HomeIntent()
        data class DeleteZone(val zoneId: String) : HomeIntent()
        object ToggleStatusCard : HomeIntent()
    }

    /**
     * UI 상태를 나타내는 State
     */
    data class HomeState(
        val isLoading: Boolean = false,
        val currentZone: ParkingZone? = null,
        val availableZones: List<ParkingZone> = emptyList(),
        val vehicles: List<com.naze.parkingfee.domain.model.vehicle.Vehicle> = emptyList(),
        val activeParkingSession: ParkingSession? = null,
        val selectedVehicle: com.naze.parkingfee.domain.model.vehicle.Vehicle? = null,
        val parkingFee: Double = 0.0,
        val parkingDuration: String = "00:00",
        val errorMessage: String? = null,
        val isParkingActive: Boolean = false,
        val isStatusCardExpanded: Boolean = true
    )

    /**
     * 일회성 이벤트를 나타내는 Effect
     */
    sealed class HomeEffect {
        data class ShowToast(val message: String) : HomeEffect()
        data class NavigateTo(val route: String) : HomeEffect()
        data class NavigateToZoneDetail(val zoneId: String) : HomeEffect()
        data class NavigateToEditZone(val zoneId: String) : HomeEffect()
        data class ShowDialog(val title: String, val message: String) : HomeEffect()
        data class ShowDeleteConfirmDialog(val zoneId: String, val zoneName: String) : HomeEffect()
        data class ShowParkingCompleteDialog(
            val zoneName: String,
            val duration: String,
            val vehicleDisplay: String?,
            val originalFee: Double?,
            val finalFee: Double,
            val hasDiscount: Boolean
        ) : HomeEffect()
        object RequestStartParkingService : HomeEffect()
        object RequestStopParkingService : HomeEffect()
    }
}
