package com.naze.parkingfee.presentation.ui.appupdate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.parkingfee.domain.repository.AppUpdateRepository
import com.naze.parkingfee.domain.usecase.appupdate.AppUpdateCheckResult
import com.naze.parkingfee.domain.usecase.appupdate.CheckAppUpdateOnLaunchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    private val checkAppUpdateOnLaunchUseCase: CheckAppUpdateOnLaunchUseCase,
    private val appUpdateRepository: AppUpdateRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AppUpdateContract.State())
    val state: StateFlow<AppUpdateContract.State> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AppUpdateContract.Effect>()
    val effect: SharedFlow<AppUpdateContract.Effect> = _effect.asSharedFlow()

    fun processIntent(intent: AppUpdateContract.Intent) {
        when (intent) {
            is AppUpdateContract.Intent.CheckOnLaunch -> checkOnLaunch(intent.currentVersionCode)
            AppUpdateContract.Intent.ClickUpdate -> openStore()
            AppUpdateContract.Intent.ClickLater -> skipOptionalUpdate()
        }
    }

    private fun checkOnLaunch(currentVersionCode: Long) {
        // 중복 호출 방지(런치 1회)
        if (_state.value.isChecking) return

        viewModelScope.launch {
            _state.update { it.copy(isChecking = true) }
            val result = checkAppUpdateOnLaunchUseCase.execute(currentVersionCode)

            when (result) {
                is AppUpdateCheckResult.ForceUpdate -> {
                    _state.update {
                        it.copy(
                            isChecking = false,
                            showForceUpdateDialog = true,
                            showOptionalUpdateDialog = false,
                            message = result.message,
                            storeUrl = result.storeUrl,
                            latestVersionCode = 0L
                        )
                    }
                }

                is AppUpdateCheckResult.OptionalUpdate -> {
                    _state.update {
                        it.copy(
                            isChecking = false,
                            showForceUpdateDialog = false,
                            showOptionalUpdateDialog = true,
                            message = result.message,
                            storeUrl = result.storeUrl,
                            latestVersionCode = result.latestVersionCode
                        )
                    }
                }

                AppUpdateCheckResult.NoUpdate -> {
                    _state.update {
                        it.copy(
                            isChecking = false,
                            showForceUpdateDialog = false,
                            showOptionalUpdateDialog = false
                        )
                    }
                }
            }
        }
    }

    private fun openStore() {
        viewModelScope.launch {
            _effect.emit(AppUpdateContract.Effect.OpenStore(_state.value.storeUrl))
        }
    }

    private fun skipOptionalUpdate() {
        val latest = _state.value.latestVersionCode
        if (latest <= 0L) {
            _state.update { it.copy(showOptionalUpdateDialog = false) }
            return
        }

        viewModelScope.launch {
            try {
                appUpdateRepository.setLastSkippedOptionalVersionCode(latest)
            } catch (_: Exception) {
                // 저장 실패는 UX에 영향 없게 무시(다음 런치에 다시 뜰 수 있음)
            }
            _state.update { it.copy(showOptionalUpdateDialog = false) }
        }
    }
}

