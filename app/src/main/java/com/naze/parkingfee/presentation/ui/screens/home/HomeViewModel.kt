package com.naze.parkingfee.presentation.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.parkingfee.domain.usecase.StartParkingUseCase
import com.naze.parkingfee.domain.usecase.StopParkingUseCase
import com.naze.parkingfee.domain.usecase.GetParkingZonesUseCase
import com.naze.parkingfee.domain.usecase.GetActiveParkingSessionUseCase
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
 * 홈 화면의 ViewModel
 * MVI 패턴에 따라 Intent를 처리하고 State를 관리합니다.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val startParkingUseCase: StartParkingUseCase,
    private val stopParkingUseCase: StopParkingUseCase,
    private val getParkingZonesUseCase: GetParkingZonesUseCase,
    private val getActiveParkingSessionUseCase: GetActiveParkingSessionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeContract.HomeState())
    val state: StateFlow<HomeContract.HomeState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeContract.HomeEffect>()
    val effect: SharedFlow<HomeContract.HomeEffect> = _effect.asSharedFlow()

    init {
        loadInitialData()
    }

    /**
     * Intent를 처리하는 메서드
     */
    fun processIntent(intent: HomeContract.HomeIntent) {
        when (intent) {
            is HomeContract.HomeIntent.StartParking -> startParking(intent.zoneId)
            is HomeContract.HomeIntent.StopParking -> stopParking(intent.sessionId)
            is HomeContract.HomeIntent.RefreshParkingInfo -> refreshParkingInfo()
            is HomeContract.HomeIntent.NavigateToSettings -> navigateToSettings()
            is HomeContract.HomeIntent.NavigateToHistory -> navigateToHistory()
            is HomeContract.HomeIntent.NavigateToAddParkingLot -> navigateToAddParkingLot()
            is HomeContract.HomeIntent.SelectZone -> selectZone(intent.zone)
        }
    }

    /**
     * 초기 데이터를 불러오는 함수입니다.
     * - 주차 구역 목록을 불러오고,
     * - 이전에 실행 중이던(아직 종료되지 않은) 주차 세션이 있으면 해당 정보를 불러옵니다.
     * 즉, 앱을 재실행하거나 홈 화면에 진입했을 때
     * 사용자가 주차를 종료하지 않았다면 그 세션 정보를 복원합니다.
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // 주차 구역 목록 조회
                val zones = getParkingZonesUseCase.execute()
                // 실행 중인(종료되지 않은) 주차 세션 조회
                val activeSession = getActiveParkingSessionUseCase.execute()

                _state.update {
                    it.copy(
                        isLoading = false,
                        availableZones = zones,
                        activeParkingSession = activeSession,
                        isParkingActive = activeSession != null // 실행 중인 세션이 있으면 true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    private fun startParking(zoneId: String) {
        viewModelScope.launch {
            try {
                val session = startParkingUseCase.execute(zoneId)
                _state.update { 
                    it.copy(
                        activeParkingSession = session,
                        isParkingActive = true,
                        errorMessage = null
                    )
                }
                _effect.emit(HomeContract.HomeEffect.ShowToast("주차가 시작되었습니다."))
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    private fun stopParking(sessionId: String) {
        viewModelScope.launch {
            try {
                stopParkingUseCase.execute(sessionId)
                _state.update { 
                    it.copy(
                        activeParkingSession = null,
                        isParkingActive = false,
                        parkingFee = 0.0,
                        parkingDuration = "00:00"
                    )
                }
                _effect.emit(HomeContract.HomeEffect.ShowToast("주차가 종료되었습니다."))
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    private fun refreshParkingInfo() {
        loadInitialData()
    }

    private fun navigateToSettings() {
        viewModelScope.launch {
            _effect.emit(HomeContract.HomeEffect.NavigateTo("settings"))
        }
    }

    private fun navigateToHistory() {
        viewModelScope.launch {
            _effect.emit(HomeContract.HomeEffect.NavigateTo("history"))
        }
    }

    private fun navigateToAddParkingLot() {
        viewModelScope.launch {
            _effect.emit(HomeContract.HomeEffect.NavigateTo("add_parking_lot"))
        }
    }

    private fun selectZone(zone: com.naze.parkingfee.domain.model.ParkingZone) {
        _state.update { it.copy(currentZone = zone) }
    }
}
