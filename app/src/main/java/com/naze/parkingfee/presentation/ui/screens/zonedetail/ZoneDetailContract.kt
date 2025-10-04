package com.naze.parkingfee.presentation.ui.screens.zonedetail

import com.naze.parkingfee.domain.model.ParkingZone

/**
 * 주차 구역 상세 화면의 MVI Contract
 */
object ZoneDetailContract {

    /**
     * 사용자 액션을 나타내는 Intent
     */
    sealed class ZoneDetailIntent {
        object LoadZone : ZoneDetailIntent()
        object SelectZone : ZoneDetailIntent()
        object NavigateToEdit : ZoneDetailIntent()
        object DeleteZone : ZoneDetailIntent()
        object NavigateBack : ZoneDetailIntent()
    }

    /**
     * UI 상태를 나타내는 State
     */
    data class ZoneDetailState(
        val isLoading: Boolean = false,
        val zone: ParkingZone? = null,
        val errorMessage: String? = null
    )

    /**
     * 일회성 이벤트를 나타내는 Effect
     */
    sealed class ZoneDetailEffect {
        data class ShowToast(val message: String) : ZoneDetailEffect()
        data class NavigateToEdit(val zoneId: String) : ZoneDetailEffect()
        object NavigateBack : ZoneDetailEffect()
        data class ShowDeleteConfirmDialog(val zoneId: String, val zoneName: String) : ZoneDetailEffect()
        object NavigateToHome : ZoneDetailEffect()
    }
}
