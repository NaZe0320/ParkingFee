package com.naze.parkingfee.presentation.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.parkingfee.data.datasource.local.blockstore.BlockStoreDataSource
import com.naze.parkingfee.domain.usecase.DeleteUserUseCase
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
    private val deleteUserUseCase: DeleteUserUseCase,
    private val blockStoreDataSource: BlockStoreDataSource
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
            is SettingsContract.SettingsIntent.ShowDeleteUserDialog -> showDeleteUserDialog()
            is SettingsContract.SettingsIntent.DeleteUser -> deleteUser()
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                // 사용자 ID 조회
                val userId = blockStoreDataSource.getOrCreateUserId()
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        notificationEnabled = true,
                        autoStopEnabled = false,
                        userId = userId
                    )
                }
            } catch (exception: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "설정 로드 실패: ${exception.message}"
                    )
                }
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

    private fun showDeleteUserDialog() {
        viewModelScope.launch {
            _state.update { it.copy(showDeleteDialog = true) }
        }
    }

    private fun deleteUser() {
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }
            
            try {
                val result = deleteUserUseCase.execute()
                if (result.isSuccess) {
                    _effect.emit(SettingsContract.SettingsEffect.UserDeleted)
                    _effect.emit(SettingsContract.SettingsEffect.ShowToast("계정이 삭제되었습니다."))
                } else {
                    _state.update { 
                        it.copy(
                            isDeleting = false,
                            errorMessage = "계정 삭제 실패: ${result.exceptionOrNull()?.message}"
                        )
                    }
                }
            } catch (exception: Exception) {
                _state.update { 
                    it.copy(
                        isDeleting = false,
                        errorMessage = "계정 삭제 중 오류 발생: ${exception.message}"
                    )
                }
            }
        }
    }
}
