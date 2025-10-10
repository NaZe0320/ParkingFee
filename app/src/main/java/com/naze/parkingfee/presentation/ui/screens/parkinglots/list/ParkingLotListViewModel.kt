package com.naze.parkingfee.presentation.ui.screens.settings.parkinglots.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.domain.usecase.GetParkingZonesUseCase
import com.naze.parkingfee.domain.usecase.DeleteParkingZoneUseCase
import com.naze.parkingfee.domain.usecase.ToggleParkingZoneFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 주차장 목록 화면의 ViewModel
 * MVI 패턴에 따라 Intent를 처리하고 State를 관리합니다.
 */
@HiltViewModel
class ParkingLotListViewModel @Inject constructor(
    private val getParkingZonesUseCase: GetParkingZonesUseCase,
    private val deleteParkingZoneUseCase: DeleteParkingZoneUseCase,
    private val toggleParkingZoneFavoriteUseCase: ToggleParkingZoneFavoriteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ParkingLotListContract.ParkingLotListState())
    val state: StateFlow<ParkingLotListContract.ParkingLotListState> = _state.asStateFlow()

    private val _effect = MutableStateFlow<ParkingLotListContract.ParkingLotListEffect?>(null)
    val effect: StateFlow<ParkingLotListContract.ParkingLotListEffect?> = _effect.asStateFlow()

    /**
     * Intent를 처리하는 메서드
     */
    fun processIntent(intent: ParkingLotListContract.ParkingLotListIntent) {
        when (intent) {
            is ParkingLotListContract.ParkingLotListIntent.LoadParkingLots -> {
                loadParkingLots()
            }
            is ParkingLotListContract.ParkingLotListIntent.NavigateToAddParkingLot -> {
                _effect.value = ParkingLotListContract.ParkingLotListEffect.NavigateToAddParkingLot
            }
            is ParkingLotListContract.ParkingLotListIntent.NavigateToEditParkingLot -> {
                _effect.value = ParkingLotListContract.ParkingLotListEffect.NavigateToEditParkingLot(intent.zoneId)
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
     * 주차장 목록을 로드합니다.
     */
    private fun loadParkingLots() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val parkingLots = getParkingZonesUseCase.execute()
                val sortedLots = sortParkingLots(parkingLots, _state.value.sortOrder)
                _state.update { 
                    it.copy(
                        parkingLots = sortedLots,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "주차장 목록을 불러오는데 실패했습니다."
                    )
                }
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
                    _effect.value = ParkingLotListContract.ParkingLotListEffect.ShowDeleteConfirmation(
                        zoneId = zoneId,
                        zoneName = zone.name
                    )
                }
            } catch (e: Exception) {
                _effect.value = ParkingLotListContract.ParkingLotListEffect.ShowToast(
                    e.message ?: "주차장 삭제에 실패했습니다."
                )
            }
        }
    }

    /**
     * 주차장 삭제를 확인하고 실행합니다.
     */
    fun confirmDeleteParkingLot(zoneId: String) {
        viewModelScope.launch {
            try {
                val success = deleteParkingZoneUseCase.invoke(zoneId)
                if (success) {
                    _effect.value = ParkingLotListContract.ParkingLotListEffect.ShowToast("주차장이 삭제되었습니다.")
                    loadParkingLots() // 목록 새로고침
                } else {
                    _effect.value = ParkingLotListContract.ParkingLotListEffect.ShowToast("주차장 삭제에 실패했습니다.")
                }
            } catch (e: Exception) {
                _effect.value = ParkingLotListContract.ParkingLotListEffect.ShowToast(
                    e.message ?: "주차장 삭제에 실패했습니다."
                )
            }
        }
    }

    /**
     * 즐겨찾기를 토글합니다.
     */
    private fun toggleFavorite(zoneId: String) {
        viewModelScope.launch {
            try {
                val success = toggleParkingZoneFavoriteUseCase.invoke(zoneId)
                if (success) {
                    loadParkingLots() // 목록 새로고침
                } else {
                    _effect.value = ParkingLotListContract.ParkingLotListEffect.ShowToast("즐겨찾기 설정에 실패했습니다.")
                }
            } catch (e: Exception) {
                _effect.value = ParkingLotListContract.ParkingLotListEffect.ShowToast(
                    e.message ?: "즐겨찾기 설정에 실패했습니다."
                )
            }
        }
    }

    /**
     * 정렬 순서를 변경합니다.
     */
    private fun changeSortOrder(sortOrder: ParkingLotListContract.SortOrder) {
        _state.update { currentState ->
            val sortedLots = sortParkingLots(currentState.parkingLots, sortOrder)
            currentState.copy(
                sortOrder = sortOrder,
                parkingLots = sortedLots
            )
        }
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

    /**
     * 주차장 목록을 새로고침합니다.
     */
    fun refreshParkingLots() {
        loadParkingLots()
    }
}
