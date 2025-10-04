package com.naze.parkingfee.presentation.ui.screens.settings.vehicles.add

import com.naze.parkingfee.domain.common.discount.DiscountEligibility
import com.naze.parkingfee.domain.model.vehicle.Vehicle
import com.naze.parkingfee.domain.model.vehicle.VehicleInfo

/**
 * 차량 등록 화면의 MVI Contract
 */
object AddVehicleContract {
    
    sealed class AddVehicleIntent {
        object Initialize : AddVehicleIntent()
        data class UpdateVehicleName(val name: String) : AddVehicleIntent()
        data class UpdatePlateNumber(val plateNumber: String) : AddVehicleIntent()
        data class ToggleCompactCarDiscount(val enabled: Boolean) : AddVehicleIntent()
        data class ToggleNationalMeritDiscount(val enabled: Boolean) : AddVehicleIntent()
        data class ToggleDisabledDiscount(val enabled: Boolean) : AddVehicleIntent()
        object OpenOcrScreen : AddVehicleIntent()
        object SaveVehicle : AddVehicleIntent()
        object NavigateBack : AddVehicleIntent()
        data class LoadVehicleForEdit(val vehicleId: String) : AddVehicleIntent()
    }
    
    data class AddVehicleState(
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val isEditMode: Boolean = false,
        val vehicleId: String? = null,
        val vehicleName: String = "",
        val plateNumber: String = "",
        val compactCarDiscount: Boolean = false,
        val nationalMeritDiscount: Boolean = false,
        val disabledDiscount: Boolean = false,
        val validationErrors: Map<String, String> = emptyMap(),
        val errorMessage: String? = null
    )
    
    sealed class AddVehicleEffect {
        data class ShowToast(val message: String) : AddVehicleEffect()
        object NavigateBack : AddVehicleEffect()
        object OpenOcrScreen : AddVehicleEffect()
        data class NavigateTo(val route: String) : AddVehicleEffect()
        data class ShowDialog(val title: String, val message: String) : AddVehicleEffect()
        data class ShowValidationError(val field: String, val message: String) : AddVehicleEffect()
    }
}
