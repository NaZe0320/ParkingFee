package com.naze.parkingfee.presentation.ui.screens.vehicledetail

import com.naze.parkingfee.domain.model.vehicle.Vehicle

/**
 * 차량 상세 화면의 MVI Contract
 */
object VehicleDetailContract {

    /**
     * 사용자 액션을 나타내는 Intent
     */
    sealed class VehicleDetailIntent {
        object LoadVehicle : VehicleDetailIntent()
        object NavigateToEdit : VehicleDetailIntent()
        object DeleteVehicle : VehicleDetailIntent()
        object NavigateBack : VehicleDetailIntent()
    }

    /**
     * UI 상태를 나타내는 State
     */
    data class VehicleDetailState(
        val isLoading: Boolean = false,
        val vehicle: Vehicle? = null,
        val errorMessage: String? = null
    )

    /**
     * 일회성 이벤트를 나타내는 Effect
     */
    sealed class VehicleDetailEffect {
        data class ShowToast(val message: String) : VehicleDetailEffect()
        data class NavigateToEdit(val vehicleId: String) : VehicleDetailEffect()
        object NavigateBack : VehicleDetailEffect()
        object ShowDeleteConfirmDialog : VehicleDetailEffect()
    }
}

