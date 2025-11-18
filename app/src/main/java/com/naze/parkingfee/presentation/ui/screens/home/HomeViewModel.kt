package com.naze.parkingfee.presentation.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.parkingfee.domain.usecase.parkingsession.StartParkingUseCase
import com.naze.parkingfee.domain.usecase.parkingsession.StopParkingUseCase
import com.naze.parkingfee.domain.usecase.parkingzone.GetParkingZonesUseCase
import com.naze.parkingfee.domain.usecase.parkingsession.GetActiveParkingSessionUseCase
import com.naze.parkingfee.domain.usecase.parkingzone.DeleteParkingZoneUseCase
import com.naze.parkingfee.domain.usecase.parkingzone.UpdateParkingZoneUseCase
import com.naze.parkingfee.presentation.ui.screens.home.components.ZoneAction
import com.naze.parkingfee.utils.TimeUtils
import com.naze.parkingfee.utils.FeeCalculator
import com.naze.parkingfee.domain.usecase.selectedvehicle.GetSelectedVehicleIdUseCase
import com.naze.parkingfee.domain.usecase.vehicle.GetVehiclesUseCase
import com.naze.parkingfee.domain.repository.VehicleRepository
import com.naze.parkingfee.domain.repository.SelectedVehicleRepository
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
    private val updateParkingZoneUseCase: UpdateParkingZoneUseCase,
    private val deleteParkingZoneUseCase: DeleteParkingZoneUseCase,
    private val getSelectedVehicleIdUseCase: GetSelectedVehicleIdUseCase,
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val vehicleRepository: VehicleRepository,
    private val selectedVehicleRepository: SelectedVehicleRepository,
    private val parkingRepository: com.naze.parkingfee.domain.repository.ParkingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeContract.HomeState())
    val state: StateFlow<HomeContract.HomeState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeContract.HomeEffect>()
    val effect: SharedFlow<HomeContract.HomeEffect> = _effect.asSharedFlow()

    // 실시간 갱신을 위한 틱커
    private var tickerJob: Job? = null

    init {
        // Repository의 선택된 주차장 ID를 구독하여 State에 반영
        viewModelScope.launch {
            parkingRepository.selectedParkingZoneId.collect { selectedId ->
                if (selectedId != null) {
                    // 선택된 ID에 해당하는 주차장을 availableZones에서 찾아서 currentZone에 설정
                    val selectedZone = _state.value.availableZones.firstOrNull { it.id == selectedId }
                    if (selectedZone != null) {
                        _state.update { it.copy(currentZone = selectedZone) }
                    }
                }
            }
        }
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
            is HomeContract.HomeIntent.SelectVehicle -> selectVehicle(intent.vehicle)
            is HomeContract.HomeIntent.RequestZoneAction -> handleZoneAction(intent.zone, intent.action)
            is HomeContract.HomeIntent.DeleteZone -> deleteZone(intent.zoneId)
            is HomeContract.HomeIntent.DeleteVehicle -> deleteVehicle(intent.vehicle)
            is HomeContract.HomeIntent.ToggleStatusCard -> toggleStatusCard()
            is HomeContract.HomeIntent.ToggleVehicleSelector -> toggleVehicleSelector()
            is HomeContract.HomeIntent.ToggleParkingZoneSelector -> toggleParkingZoneSelector()
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

                // 차량 목록 조회
                val vehicles = getVehiclesUseCase.execute()
                
                // 선택된 차량 로드
                val selectedVehicleId = getSelectedVehicleIdUseCase.execute().first()
                val selectedVehicle = selectedVehicleId?.let { vehicleRepository.getVehicleById(it) }

                _state.update {
                    it.copy(
                        isLoading = false,
                        availableZones = zones,
                        vehicles = vehicles,
                        activeParkingSession = activeSession,
                        selectedVehicle = selectedVehicle,
                        isParkingActive = activeSession != null, // 실행 중인 세션이 있으면 true
                        activeZoneName = activeSession?.let { session ->
                            zones.firstOrNull { zone -> zone.id == session.zoneId }?.name
                        }
                    )
                }
                
                // zones 로드 후 선택된 주차장 ID 확인하여 currentZone 설정
                val selectedZoneId = parkingRepository.selectedParkingZoneId.value
                if (selectedZoneId != null) {
                    val selectedZone = zones.firstOrNull { it.id == selectedZoneId }
                    if (selectedZone != null) {
                        _state.update { it.copy(currentZone = selectedZone) }
                    }
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
                val zone = _state.value.availableZones.firstOrNull { it.id == zoneId }
                _state.update { 
                    it.copy(
                        activeParkingSession = session,
                        isParkingActive = true,
                        errorMessage = null,
                        activeZoneName = zone?.name
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
                        parkingDuration = "00:00",
                        activeZoneName = null
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

    /**
     * 주차장을 선택합니다.
     * 이미 선택된 주차장을 다시 선택하면 선택을 해제합니다.
     * 선택 시 updatedAt을 현재 시간으로 업데이트하여 최근 사용 추적
     */
    private fun selectZone(zone: com.naze.parkingfee.domain.model.ParkingZone) {
        viewModelScope.launch {
            try {
                val currentSelectedId = parkingRepository.selectedParkingZoneId.value
                if (currentSelectedId == zone.id) {
                    // 이미 선택된 주차장을 다시 선택하면 선택 해제
                    parkingRepository.setSelectedParkingZoneId(null)
                    _state.update { 
                        it.copy(currentZone = null)
                    }
                } else {
                    // 새로운 주차장 선택
                    parkingRepository.setSelectedParkingZoneId(zone.id)
                    
                    // updatedAt을 현재 시간으로 업데이트
                    val updatedZone = zone.copy(updatedAt = System.currentTimeMillis())
                    updateParkingZoneUseCase.execute(updatedZone)
                    
                    _state.update { 
                        it.copy(currentZone = updatedZone)
                    }
                }
            } catch (e: Exception) {
                // 에러가 발생해도 UI 상태는 업데이트 (사용자 경험 우선)
                val currentSelectedId = parkingRepository.selectedParkingZoneId.value
                if (currentSelectedId == zone.id) {
                    parkingRepository.setSelectedParkingZoneId(null)
                    _state.update { 
                        it.copy(currentZone = null)
                    }
                } else {
                    parkingRepository.setSelectedParkingZoneId(zone.id)
                    _state.update { 
                        it.copy(currentZone = zone)
                    }
                }
            }
        }
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
     * 차량을 삭제합니다.
     */
    private fun deleteVehicle(vehicle: com.naze.parkingfee.domain.model.vehicle.Vehicle) {
        viewModelScope.launch {
            try {
                vehicleRepository.deleteVehicle(vehicle.id)
                
                // 현재 선택된 차량이 삭제된 차량이면 선택 해제
                val currentVehicle = _state.value.selectedVehicle
                if (currentVehicle?.id == vehicle.id) {
                    selectedVehicleRepository.setSelectedVehicleId(null)
                    _state.update { it.copy(selectedVehicle = null) }
                }
                
                // 차량 목록 새로고침
                refreshParkingInfo()
                _effect.emit(HomeContract.HomeEffect.ShowToast("차량이 삭제되었습니다."))
            } catch (e: Exception) {
                _effect.emit(HomeContract.HomeEffect.ShowToast("삭제 중 오류가 발생했습니다: ${e.message}"))
            }
        }
    }
    
    /**
     * 차량을 선택합니다.
     */
    private fun selectVehicle(vehicle: com.naze.parkingfee.domain.model.vehicle.Vehicle) {
        viewModelScope.launch {
            try {
                val currentVehicle = _state.value.selectedVehicle
                
                // 이미 선택된 차량을 다시 클릭하면 선택 해제
                if (currentVehicle?.id == vehicle.id) {
                    selectedVehicleRepository.setSelectedVehicleId(null)
                    _state.update { it.copy(selectedVehicle = null) }
                    return@launch
                }
                
                // 선택된 차량을 저장
                selectedVehicleRepository.setSelectedVehicleId(vehicle.id)
                
                // 상태 업데이트
                _state.update { 
                    it.copy(selectedVehicle = vehicle)
                }
                
                // 활성 주차 세션이 있으면 요금 재계산
                val session = _state.value.activeParkingSession
                if (session != null) {
                    val zone = resolveZone(session.zoneId)
                    if (zone != null) {
                        val now = TimeUtils.getCurrentTimestamp()
                        val fee = FeeCalculator.calculateFeeForZone(session.startTime, now, zone, vehicle)
                        _state.update { 
                            it.copy(parkingFee = fee)
                        }
                    }
                }
                
                _effect.emit(HomeContract.HomeEffect.ShowToast("${vehicle.displayName}이(가) 선택되었습니다."))
            } catch (e: Exception) {
                _effect.emit(HomeContract.HomeEffect.ShowToast("차량 선택 중 오류가 발생했습니다: ${e.message}"))
            }
        }
    }
    
    /**
     * 상태 카드 접기/펼치기를 토글합니다.
     */
    private fun toggleStatusCard() {
        _state.update { 
            it.copy(isStatusCardExpanded = !it.isStatusCardExpanded)
        }
    }
    
    /**
     * 차량 선택 섹션 접기/펼치기를 토글합니다.
     */
    private fun toggleVehicleSelector() {
        _state.update { 
            it.copy(isVehicleSelectorExpanded = !it.isVehicleSelectorExpanded)
        }
    }
    
    /**
     * 주차장 선택 섹션 접기/펼치기를 토글합니다.
     */
    private fun toggleParkingZoneSelector() {
        _state.update { 
            it.copy(isParkingZoneSelectorExpanded = !it.isParkingZoneSelectorExpanded)
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
