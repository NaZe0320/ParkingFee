package com.naze.parkingfee.presentation.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.parkingfee.domain.usecase.StartParkingUseCase
import com.naze.parkingfee.domain.usecase.StopParkingUseCase
import com.naze.parkingfee.domain.usecase.GetParkingZonesUseCase
import com.naze.parkingfee.domain.usecase.GetActiveParkingSessionUseCase
import com.naze.parkingfee.domain.usecase.DeleteParkingZoneUseCase
import com.naze.parkingfee.presentation.ui.screens.home.components.ZoneAction
import com.naze.parkingfee.utils.TimeUtils
import com.naze.parkingfee.utils.FeeCalculator
import com.naze.parkingfee.domain.usecase.GetSelectedVehicleIdUseCase
import com.naze.parkingfee.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.first
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
    private val getActiveParkingSessionUseCase: GetActiveParkingSessionUseCase,
    private val deleteParkingZoneUseCase: DeleteParkingZoneUseCase,
    private val getSelectedVehicleIdUseCase: GetSelectedVehicleIdUseCase,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeContract.HomeState())
    val state: StateFlow<HomeContract.HomeState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeContract.HomeEffect>()
    val effect: SharedFlow<HomeContract.HomeEffect> = _effect.asSharedFlow()

    // 실시간 갱신을 위한 틱커
    private var tickerJob: Job? = null

    // init 블록 제거 - 화면 진입 시마다 LaunchedEffect로 새로고침

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
            is HomeContract.HomeIntent.RequestZoneAction -> handleZoneAction(intent.zone, intent.action)
            is HomeContract.HomeIntent.DeleteZone -> deleteZone(intent.zoneId)
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

                // 선택된 차량 로드
                val selectedVehicleId = getSelectedVehicleIdUseCase.execute().first()
                val selectedVehicle = selectedVehicleId?.let { vehicleRepository.getVehicleById(it) }

                _state.update {
                    it.copy(
                        isLoading = false,
                        availableZones = zones,
                        activeParkingSession = activeSession,
                        selectedVehicle = selectedVehicle,
                        isParkingActive = activeSession != null // 실행 중인 세션이 있으면 true
                    )
                }
                
                // 활성 세션이 있으면 틱커 시작
                if (activeSession != null) {
                    startTicker()
                } else {
                    stopTicker()
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
                startTicker()
                _effect.emit(HomeContract.HomeEffect.ShowToast("주차가 시작되었습니다."))
                _effect.emit(HomeContract.HomeEffect.RequestStartParkingService)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    private fun stopParking(sessionId: String) {
        viewModelScope.launch {
            try {
                val session = stopParkingUseCase.execute(sessionId)
                stopTicker()
                
                // 주차 완료 정보 수집
                val zone = resolveZone(session.zoneId)
                val zoneName = zone?.name ?: "알 수 없는 구역"
                val duration = TimeUtils.formatDuration(session.endTime!! - session.startTime)
                val vehicleDisplay = _state.value.selectedVehicle?.let { v ->
                    if (v.hasPlateNumber) "${v.displayName}(${v.displayPlateNumber})" else v.displayName
                }
                
                // 할인 적용된 요금 계산
                val feeResult = FeeCalculator.calculateFeeForZoneResult(
                    session.startTime,
                    session.endTime,
                    zone ?: return@launch,
                    _state.value.selectedVehicle
                )
                
                _state.update { 
                    it.copy(
                        activeParkingSession = null,
                        isParkingActive = false,
                        parkingFee = 0.0,
                        parkingDuration = "00:00"
                    )
                }
                
                // 주차 완료 다이얼로그 표시
                _effect.emit(HomeContract.HomeEffect.ShowParkingCompleteDialog(
                    zoneName = zoneName,
                    duration = duration,
                    vehicleDisplay = vehicleDisplay,
                    originalFee = feeResult.original,
                    finalFee = feeResult.discounted,
                    hasDiscount = feeResult.hasDiscount
                ))
                
                _effect.emit(HomeContract.HomeEffect.RequestStopParkingService)
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
    
    private fun handleZoneAction(zone: com.naze.parkingfee.domain.model.ParkingZone, action: ZoneAction) {
        viewModelScope.launch {
            when (action) {
                ZoneAction.Detail -> {
                    _effect.emit(HomeContract.HomeEffect.NavigateToZoneDetail(zone.id))
                }
                ZoneAction.Edit -> {
                    _effect.emit(HomeContract.HomeEffect.NavigateToEditZone(zone.id))
                }
                ZoneAction.Delete -> {
                    _effect.emit(HomeContract.HomeEffect.ShowDeleteConfirmDialog(zone.id, zone.name))
                }
            }
        }
    }
    
    private fun deleteZone(zoneId: String) {
        viewModelScope.launch {
            try {
                // 활성 세션이 있는지 확인
                val activeSession = _state.value.activeParkingSession
                if (activeSession?.zoneId == zoneId) {
                    _effect.emit(HomeContract.HomeEffect.ShowToast("현재 주차 중인 구역은 삭제할 수 없습니다."))
                    return@launch
                }
                
                deleteParkingZoneUseCase.execute(zoneId)
                
                // 현재 선택된 구역이 삭제된 구역이면 선택 해제
                val currentZone = _state.value.currentZone
                if (currentZone?.id == zoneId) {
                    _state.update { it.copy(currentZone = null) }
                }
                
                // 구역 목록 새로고침
                refreshParkingInfo()
                _effect.emit(HomeContract.HomeEffect.ShowToast("주차 구역이 삭제되었습니다."))
            } catch (e: Exception) {
                _effect.emit(HomeContract.HomeEffect.ShowToast("삭제 중 오류가 발생했습니다: ${e.message}"))
            }
        }
    }
    
    /**
     * 실시간 갱신을 위한 틱커 시작
     */
    private fun startTicker() {
        if (tickerJob?.isActive == true) return
        
        tickerJob = viewModelScope.launch {
            while (coroutineContext.isActive) {
                val session = _state.value.activeParkingSession
                if (_state.value.isParkingActive && session != null) {
                    val now = TimeUtils.getCurrentTimestamp()
                    val duration = TimeUtils.formatDuration(now - session.startTime)
                    val zone = resolveZone(session.zoneId)
                    val fee = if (zone != null) {
                        // 차량 선택 반영하여 할인 포함 계산
                        FeeCalculator.calculateFeeForZone(session.startTime, now, zone, _state.value.selectedVehicle)
                    } else {
                        0.0
                    }
                    
                    _state.update { 
                        it.copy(
                            parkingDuration = duration,
                            parkingFee = fee
                        )
                    }
                }
                delay(60000) // 60초(1분)마다 갱신
            }
        }
    }
    
    /**
     * 틱커 정지
     */
    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }
    
    /**
     * 주차 구역 ID로 주차 구역 조회
     */
    private fun resolveZone(zoneId: String): com.naze.parkingfee.domain.model.ParkingZone? {
        return _state.value.currentZone?.takeIf { it.id == zoneId }
            ?: _state.value.availableZones.firstOrNull { it.id == zoneId }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopTicker()
    }
}
