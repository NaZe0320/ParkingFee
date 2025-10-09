package com.naze.parkingfee.presentation.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 설정 화면의 ViewModel
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    // UseCase 주입 예정
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsContract.SettingsState())
    val state: StateFlow<SettingsContract.SettingsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SettingsContract.SettingsEffect>()
    val effect: SharedFlow<SettingsContract.SettingsEffect> = _effect.asSharedFlow()

    init {
        loadSettings()
    }

    fun processIntent(intent: SettingsContract.SettingsIntent) {
        when (intent) {
            is SettingsContract.SettingsIntent.LoadSettings -> loadSettings()
            is SettingsContract.SettingsIntent.UpdateNotificationSetting -> updateNotificationSetting(intent.enabled)
            is SettingsContract.SettingsIntent.UpdateAutoStopSetting -> updateAutoStopSetting(intent.enabled)
            is SettingsContract.SettingsIntent.NavigateToVehicleManagement -> navigateToVehicleManagement()
            is SettingsContract.SettingsIntent.NavigateToParkingLotManagement -> navigateToParkingLotManagement()
            is SettingsContract.SettingsIntent.NavigateBack -> navigateBack()
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // 설정 로드 로직
            _state.update { 
                it.copy(
                    isLoading = false,
                    notificationEnabled = true,
                    autoStopEnabled = false
                )
            }
        }
    }

    private fun updateNotificationSetting(enabled: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(notificationEnabled = enabled) }
            _effect.emit(SettingsContract.SettingsEffect.ShowToast("알림 설정이 변경되었습니다."))
        }
    }

    private fun updateAutoStopSetting(enabled: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(autoStopEnabled = enabled) }
            _effect.emit(SettingsContract.SettingsEffect.ShowToast("자동 종료 설정이 변경되었습니다."))
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.emit(SettingsContract.SettingsEffect.NavigateBack)
        }
    }
    
    private fun navigateToVehicleManagement() {
        viewModelScope.launch {
            _effect.emit(SettingsContract.SettingsEffect.NavigateToVehicleManagement)
        }
    }
    
    private fun navigateToParkingLotManagement() {
        viewModelScope.launch {
            _effect.emit(SettingsContract.SettingsEffect.NavigateToParkingLotManagement)
        }
    }
}
