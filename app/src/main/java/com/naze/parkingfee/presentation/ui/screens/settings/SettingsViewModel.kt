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
            is SettingsContract.SettingsIntent.OpenPrivacyPolicy -> openPrivacyPolicy()
            is SettingsContract.SettingsIntent.OpenTermsOfService -> openTermsOfService()
            is SettingsContract.SettingsIntent.OpenOpenSourceLicenses -> openOpenSourceLicenses()
            is SettingsContract.SettingsIntent.RequestDeleteAccount -> requestDeleteAccount()
            is SettingsContract.SettingsIntent.ConfirmDeleteAccount -> confirmDeleteAccount()
            is SettingsContract.SettingsIntent.DismissDeleteDialog -> dismissDeleteDialog()
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
                    notificationEnabled = true
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

    private fun openPrivacyPolicy() {
        viewModelScope.launch {
            // TODO: 실제 개인정보처리방침 URL로 변경 필요
            _effect.emit(SettingsContract.SettingsEffect.OpenUrl("https://www.example.com/privacy"))
        }
    }
    
    private fun openTermsOfService() {
        viewModelScope.launch {
            // TODO: 실제 이용약관 URL로 변경 필요
            _effect.emit(SettingsContract.SettingsEffect.OpenUrl("https://www.example.com/terms"))
        }
    }
    
    private fun openOpenSourceLicenses() {
        viewModelScope.launch {
            // TODO: 실제 오픈소스 라이센스 화면으로 변경 필요
            // 예시: Google OSS Licenses Plugin 사용 시
            _effect.emit(SettingsContract.SettingsEffect.OpenUrl("https://www.example.com/licenses"))
        }
    }
    
    private fun requestDeleteAccount() {
        viewModelScope.launch {
            _state.update { it.copy(showDeleteDialog = true) }
        }
    }
    
    private fun confirmDeleteAccount() {
        viewModelScope.launch {
            _state.update { it.copy(showDeleteDialog = false, isLoading = true) }
            
            // TODO: 실제 계정 삭제 로직 구현 (UseCase 호출)
            // 예시: deleteAccountUseCase.execute()
            
            _effect.emit(SettingsContract.SettingsEffect.ShowToast("계정이 삭제되었습니다."))
            _state.update { it.copy(isLoading = false) }
            
            // 로그아웃 및 로그인 화면으로 이동 로직 추가 필요
        }
    }
    
    private fun dismissDeleteDialog() {
        viewModelScope.launch {
            _state.update { it.copy(showDeleteDialog = false) }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.emit(SettingsContract.SettingsEffect.NavigateBack)
        }
    }
}
