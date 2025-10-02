package com.naze.parkingfee.presentation.ui.screens.home

import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.domain.model.ParkingSession

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
    }

    /**
     * UI 상태를 나타내는 State
     */
    data class HomeState(
        val isLoading: Boolean = false,
        val currentZone: ParkingZone? = null,
        val availableZones: List<ParkingZone> = emptyList(),
        val activeParkingSession: ParkingSession? = null,
        val parkingFee: Double = 0.0,
        val parkingDuration: String = "00:00",
        val errorMessage: String? = null,
        val isParkingActive: Boolean = false
    )

    /**
     * 일회성 이벤트를 나타내는 Effect
     */
    sealed class HomeEffect {
        data class ShowToast(val message: String) : HomeEffect()
        data class NavigateTo(val route: String) : HomeEffect()
        data class ShowDialog(val title: String, val message: String) : HomeEffect()
        object RequestStartParkingService : HomeEffect()
        object RequestStopParkingService : HomeEffect()
    }
}
