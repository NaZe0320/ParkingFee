package com.naze.parkingfee.presentation.ui.screens.settings

/**
 * 설정 화면의 MVI Contract
 */
object SettingsContract {

    sealed class SettingsIntent {
        object LoadSettings : SettingsIntent()
        data class UpdateNotificationSetting(val enabled: Boolean) : SettingsIntent()
        data class UpdateAutoStopSetting(val enabled: Boolean) : SettingsIntent()
        object NavigateToVehicleManagement : SettingsIntent()
        object NavigateToParkingLotManagement : SettingsIntent()
        object NavigateBack : SettingsIntent()
        object ShowDeleteUserDialog : SettingsIntent()
        object DeleteUser : SettingsIntent()
    }

    data class SettingsState(
        val isLoading: Boolean = false,
        val notificationEnabled: Boolean = true,
        val autoStopEnabled: Boolean = false,
        val errorMessage: String? = null,
        val userId: String? = null,
        val showDeleteDialog: Boolean = false,
        val isDeleting: Boolean = false
    )

    sealed class SettingsEffect {
        data class ShowToast(val message: String) : SettingsEffect()
        object NavigateBack : SettingsEffect()
        object NavigateToVehicleManagement : SettingsEffect()
        object NavigateToParkingLotManagement : SettingsEffect()
        object UserDeleted : SettingsEffect()
    }
}
