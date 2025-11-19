package com.naze.parkingfee.presentation.ui.screens.settings.parkinglots.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.domain.repository.ParkingRepository
import com.naze.parkingfee.domain.usecase.parkingzone.DeleteParkingZoneUseCase
import com.naze.parkingfee.domain.usecase.parkingzone.DeleteZoneResult
import com.naze.parkingfee.domain.usecase.parkingzone.ToggleParkingZoneFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 주차장 목록 화면의 ViewModel
 * MVI 패턴에 따라 Intent를 처리하고 State를 관리합니다.
 */
@HiltViewModel
class ParkingLotListViewModel @Inject constructor(
    private val parkingRepository: ParkingRepository,
    private val deleteParkingZoneUseCase: DeleteParkingZoneUseCase,
    private val toggleParkingZoneFavoriteUseCase: ToggleParkingZoneFavoriteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ParkingLotListContract.ParkingLotListState())
    val state: StateFlow<ParkingLotListContract.ParkingLotListState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ParkingLotListContract.ParkingLotListEffect>()
    val effect: SharedFlow<ParkingLotListContract.ParkingLotListEffect> = _effect.asSharedFlow()
    
    init {
        // 주차장 목록 Flow 구독 (데이터베이스 변경 시 자동 업데이트)
        viewModelScope.launch {
            combine(
                parkingRepository.observeParkingZones(),
                _state.map { it.sortOrder }.distinctUntilChanged()
            ) { parkingLots, sortOrder ->
                sortParkingLots(parkingLots, sortOrder)
            }.collect { sortedLots ->
                _state.update { it.copy(parkingLots = sortedLots, isLoading = false) }
            }
        }
        
        // Repository의 선택된 주차장 ID를 구독하여 State에 반영
        viewModelScope.launch {
            parkingRepository.selectedParkingZoneId.collect { selectedId ->
                _state.update { it.copy(selectedParkingLotId = selectedId) }
            }
        }
    }

    /**
     * Intent를 처리하는 메서드
     */
    fun processIntent(intent: ParkingLotListContract.ParkingLotListIntent) {
        when (intent) {
            is ParkingLotListContract.ParkingLotListIntent.NavigateToAddParkingLot -> {
                viewModelScope.launch {
                    _effect.emit(ParkingLotListContract.ParkingLotListEffect.NavigateToAddParkingLot)
                }
            }
            is ParkingLotListContract.ParkingLotListIntent.NavigateToEditParkingLot -> {
                viewModelScope.launch {
                    _effect.emit(ParkingLotListContract.ParkingLotListEffect.NavigateToEditParkingLot(intent.zoneId))
                }
            }
            is ParkingLotListContract.ParkingLotListIntent.NavigateToDetailParkingLot -> {
                viewModelScope.launch {
                    _effect.emit(ParkingLotListContract.ParkingLotListEffect.NavigateToDetailParkingLot(intent.zoneId))
                }
            }
            is ParkingLotListContract.ParkingLotListIntent.SelectParkingLot -> {
                selectParkingLot(intent.zoneId)
            }
            is ParkingLotListContract.ParkingLotListIntent.DeleteParkingLot -> {
                deleteParkingLot(intent.zoneId)
            }
            is ParkingLotListContract.ParkingLotListIntent.ToggleFavorite -> {
                toggleFavorite(intent.zoneId)
            }
            is ParkingLotListContract.ParkingLotListIntent.ChangeSortOrder -> {
                changeSortOrder(intent.sortOrder)
            }
        }
    }
    
    /**
     * 주차장을 선택합니다.
     * 이미 선택된 주차장을 다시 선택하면 선택을 해제합니다.
     */
    private fun selectParkingLot(zoneId: String) {
        viewModelScope.launch {
            val currentSelectedId = parkingRepository.selectedParkingZoneId.value
            if (currentSelectedId == zoneId) {
                // 이미 선택된 주차장을 다시 선택하면 선택 해제
                parkingRepository.setSelectedParkingZoneId(null)
            } else {
                // 새로운 주차장 선택
                parkingRepository.setSelectedParkingZoneId(zoneId)
            }
        }
    }


    /**
     * 주차장을 삭제합니다.
     */
    private fun deleteParkingLot(zoneId: String) {
        viewModelScope.launch {
            try {
                val zone = _state.value.parkingLots.find { it.id == zoneId }
                if (zone != null) {
                    _effect.emit(ParkingLotListContract.ParkingLotListEffect.ShowDeleteConfirmation(
                        zoneId = zoneId,
                        zoneName = zone.name
                    ))
                }
            } catch (e: Exception) {
                _effect.emit(ParkingLotListContract.ParkingLotListEffect.ShowToast(
                    e.message ?: "주차장 삭제에 실패했습니다."
                ))
            }
        }
    }

    /**
     * 주차장 삭제를 확인하고 실행합니다.
     */
    fun confirmDeleteParkingLot(zoneId: String) {
        viewModelScope.launch {
            when (val result = deleteParkingZoneUseCase.execute(zoneId)) {
                is DeleteZoneResult.Success -> {
                    _effect.emit(ParkingLotListContract.ParkingLotListEffect.ShowToast("주차장이 삭제되었습니다."))
                    // Flow가 자동으로 목록을 업데이트합니다
                }
                is DeleteZoneResult.ZoneInUse -> {
                    _effect.emit(ParkingLotListContract.ParkingLotListEffect.ShowToast("현재 주차 중인 구역은 삭제할 수 없습니다."))
                }
                is DeleteZoneResult.Error -> {
                    _effect.emit(ParkingLotListContract.ParkingLotListEffect.ShowToast(
                        "주차장 삭제에 실패했습니다: ${result.message}"
                    ))
                }
            }
        }
    }

    /**
     * 즐겨찾기를 토글합니다.
     */
    private fun toggleFavorite(zoneId: String) {
        viewModelScope.launch {
            try {
                val success = toggleParkingZoneFavoriteUseCase.execute(zoneId)
                if (success) {
                    // Flow가 자동으로 목록을 업데이트합니다
                } else {
                    _effect.emit(ParkingLotListContract.ParkingLotListEffect.ShowToast("즐겨찾기 설정에 실패했습니다."))
                }
            } catch (e: Exception) {
                _effect.emit(ParkingLotListContract.ParkingLotListEffect.ShowToast(
                    e.message ?: "즐겨찾기 설정에 실패했습니다."
                ))
            }
        }
    }

    /**
     * 정렬 순서를 변경합니다.
     * Flow가 자동으로 재정렬합니다.
     */
    private fun changeSortOrder(sortOrder: ParkingLotListContract.SortOrder) {
        _state.update { it.copy(sortOrder = sortOrder) }
    }

    /**
     * 주차장 목록을 정렬합니다.
     */
    private fun sortParkingLots(
        parkingLots: List<ParkingZone>, 
        sortOrder: ParkingLotListContract.SortOrder
    ): List<ParkingZone> {
        return when (sortOrder) {
            ParkingLotListContract.SortOrder.NAME -> {
                parkingLots.sortedBy { it.name }
            }
            ParkingLotListContract.SortOrder.RECENT -> {
                parkingLots.sortedByDescending { it.updatedAt }
            }
            ParkingLotListContract.SortOrder.FAVORITE -> {
                parkingLots.sortedWith(
                    compareByDescending<ParkingZone> { it.isFavorite }
                        .thenBy { it.name }
                )
            }
        }
    }

}
