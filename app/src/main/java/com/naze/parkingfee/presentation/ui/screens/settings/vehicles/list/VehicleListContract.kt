package com.naze.parkingfee.presentation.ui.screens.settings.vehicles.list

/**
 * 차량 목록 화면의 MVI Contract
 */
object VehicleListContract {
    
    sealed class VehicleListIntent {
        object LoadVehicles : VehicleListIntent()
        object NavigateToAddVehicle : VehicleListIntent()
        data class DeleteVehicle(val vehicleId: String) : VehicleListIntent()
        data class NavigateToEditVehicle(val vehicleId: String) : VehicleListIntent()
        object NavigateBack : VehicleListIntent()
    }
    
    data class VehicleListState(
        val isLoading: Boolean = false,
        val vehicles: List<com.naze.parkingfee.domain.model.vehicle.Vehicle> = emptyList(),
        val canAddVehicle: Boolean = true, // 최대 3대 제한
        val errorMessage: String? = null
    )
    
    sealed class VehicleListEffect {
        data class ShowToast(val message: String) : VehicleListEffect()
        object NavigateBack : VehicleListEffect()
        object NavigateToAddVehicle : VehicleListEffect()
        data class NavigateToEditVehicle(val vehicleId: String) : VehicleListEffect()
        data class ShowDeleteConfirmation(val vehicleId: String, val vehicleName: String) : VehicleListEffect()
    }
}
