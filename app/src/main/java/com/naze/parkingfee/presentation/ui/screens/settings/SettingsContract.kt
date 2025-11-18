package com.naze.parkingfee.presentation.ui.screens.settings

/**
 * 설정 화면의 MVI Contract
 */
object SettingsContract {

    sealed class SettingsIntent {
        object LoadSettings : SettingsIntent()
        data class UpdateNotificationSetting(val enabled: Boolean) : SettingsIntent()
        object OpenPrivacyPolicy : SettingsIntent()
        object OpenTermsOfService : SettingsIntent()
        object OpenOpenSourceLicenses : SettingsIntent()
        object RequestDeleteAccount : SettingsIntent()
        object ConfirmDeleteAccount : SettingsIntent()
        object DismissDeleteDialog : SettingsIntent()
        object NavigateBack : SettingsIntent()
    }

    data class SettingsState(
        val isLoading: Boolean = false,
        val notificationEnabled: Boolean = true,
        val showDeleteDialog: Boolean = false,
        val appVersion: String = "1.0.0",
        val errorMessage: String? = null
    )

    sealed class SettingsEffect {
        data class ShowToast(val message: String) : SettingsEffect()
        data class OpenUrl(val url: String) : SettingsEffect()
        object NavigateBack : SettingsEffect()
    }
}
