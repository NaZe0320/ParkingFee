package com.naze.parkingfee.presentation.ui.screens.zonedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.parkingfee.domain.usecase.parkingzone.GetParkingZoneByIdUseCase
import com.naze.parkingfee.domain.usecase.parkingzone.DeleteParkingZoneUseCase
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
 * 주차 구역 상세 화면의 ViewModel
 */
@HiltViewModel
class ZoneDetailViewModel @Inject constructor(
    private val getParkingZoneByIdUseCase: GetParkingZoneByIdUseCase,
    private val deleteParkingZoneUseCase: DeleteParkingZoneUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ZoneDetailContract.ZoneDetailState())
    val state: StateFlow<ZoneDetailContract.ZoneDetailState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ZoneDetailContract.ZoneDetailEffect>()
    val effect: SharedFlow<ZoneDetailContract.ZoneDetailEffect> = _effect.asSharedFlow()

    private var zoneId: String? = null

    /**
     * Intent를 처리하는 메서드
     */
    fun processIntent(intent: ZoneDetailContract.ZoneDetailIntent) {
        when (intent) {
            is ZoneDetailContract.ZoneDetailIntent.LoadZone -> loadZone()
            is ZoneDetailContract.ZoneDetailIntent.SelectZone -> selectZone()
            is ZoneDetailContract.ZoneDetailIntent.NavigateToEdit -> navigateToEdit()
            is ZoneDetailContract.ZoneDetailIntent.DeleteZone -> deleteZone()
            is ZoneDetailContract.ZoneDetailIntent.NavigateBack -> navigateBack()
        }
    }

    /**
     * 구역 ID 설정
     */
    fun setZoneId(zoneId: String) {
        this.zoneId = zoneId
        processIntent(ZoneDetailContract.ZoneDetailIntent.LoadZone)
    }

    private fun loadZone() {
        val currentZoneId = zoneId ?: return
        
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, errorMessage = null) }
                
                val zone = getParkingZoneByIdUseCase.execute(currentZoneId)
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        zone = zone,
                        errorMessage = if (zone == null) "구역을 찾을 수 없습니다." else null
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "구역을 불러오는 중 오류가 발생했습니다."
                    )
                }
            }
        }
    }

    private fun selectZone() {
        viewModelScope.launch {
            _effect.emit(ZoneDetailContract.ZoneDetailEffect.ShowToast("구역이 선택되었습니다."))
            _effect.emit(ZoneDetailContract.ZoneDetailEffect.NavigateBack)
        }
    }

    private fun navigateToEdit() {
        val currentZoneId = zoneId ?: return
        
        viewModelScope.launch {
            _effect.emit(ZoneDetailContract.ZoneDetailEffect.NavigateToEdit(currentZoneId))
        }
    }

    private fun deleteZone() {
        val currentZoneId = zoneId ?: return
        val zoneName = _state.value.zone?.name ?: "알 수 없는 구역"
        
        viewModelScope.launch {
            _effect.emit(ZoneDetailContract.ZoneDetailEffect.ShowDeleteConfirmDialog(currentZoneId, zoneName))
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.emit(ZoneDetailContract.ZoneDetailEffect.NavigateBack)
        }
    }

    /**
     * 구역 삭제 실행
     */
    fun confirmDeleteZone(zoneId: String) {
        viewModelScope.launch {
            try {
                deleteParkingZoneUseCase.execute(zoneId)
                _effect.emit(ZoneDetailContract.ZoneDetailEffect.ShowToast("구역이 삭제되었습니다."))
                _effect.emit(ZoneDetailContract.ZoneDetailEffect.NavigateToHome)
            } catch (e: Exception) {
                _effect.emit(ZoneDetailContract.ZoneDetailEffect.ShowToast("삭제 중 오류가 발생했습니다: ${e.message}"))
            }
        }
    }
}
