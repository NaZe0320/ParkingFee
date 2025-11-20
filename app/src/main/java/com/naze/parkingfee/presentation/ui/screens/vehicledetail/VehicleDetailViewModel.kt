package com.naze.parkingfee.presentation.ui.screens.vehicledetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.parkingfee.domain.usecase.vehicle.GetVehicleByIdUseCase
import com.naze.parkingfee.domain.usecase.vehicle.DeleteVehicleUseCase
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
 * 차량 상세 화면의 ViewModel
 */
@HiltViewModel
class VehicleDetailViewModel @Inject constructor(
    private val getVehicleByIdUseCase: GetVehicleByIdUseCase,
    private val deleteVehicleUseCase: DeleteVehicleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(VehicleDetailContract.VehicleDetailState())
    val state: StateFlow<VehicleDetailContract.VehicleDetailState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<VehicleDetailContract.VehicleDetailEffect>()
    val effect: SharedFlow<VehicleDetailContract.VehicleDetailEffect> = _effect.asSharedFlow()

    private var vehicleId: String? = null

    /**
     * Intent를 처리하는 메서드
     */
    fun processIntent(intent: VehicleDetailContract.VehicleDetailIntent) {
        when (intent) {
            is VehicleDetailContract.VehicleDetailIntent.LoadVehicle -> loadVehicle()
            is VehicleDetailContract.VehicleDetailIntent.NavigateToEdit -> navigateToEdit()
            is VehicleDetailContract.VehicleDetailIntent.DeleteVehicle -> deleteVehicle()
            is VehicleDetailContract.VehicleDetailIntent.NavigateBack -> navigateBack()
        }
    }

    /**
     * 차량 ID 설정
     */
    fun setVehicleId(vehicleId: String) {
        this.vehicleId = vehicleId
        processIntent(VehicleDetailContract.VehicleDetailIntent.LoadVehicle)
    }

    private fun loadVehicle() {
        val currentVehicleId = vehicleId ?: return
        
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, errorMessage = null) }
                
                val vehicle = getVehicleByIdUseCase.execute(currentVehicleId)
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        vehicle = vehicle,
                        errorMessage = if (vehicle == null) "차량을 찾을 수 없습니다." else null
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "차량을 불러오는 중 오류가 발생했습니다."
                    )
                }
            }
        }
    }

    private fun navigateToEdit() {
        val currentVehicleId = vehicleId ?: return
        
        viewModelScope.launch {
            _effect.emit(VehicleDetailContract.VehicleDetailEffect.NavigateToEdit(currentVehicleId))
        }
    }

    private fun deleteVehicle() {
        viewModelScope.launch {
            _effect.emit(VehicleDetailContract.VehicleDetailEffect.ShowDeleteConfirmDialog)
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.emit(VehicleDetailContract.VehicleDetailEffect.NavigateBack)
        }
    }

    /**
     * 차량 삭제 실행
     */
    fun confirmDeleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            try {
                deleteVehicleUseCase.execute(vehicleId)
                _effect.emit(VehicleDetailContract.VehicleDetailEffect.ShowToast("차량이 삭제되었습니다."))
                _effect.emit(VehicleDetailContract.VehicleDetailEffect.NavigateBack)
            } catch (e: Exception) {
                _effect.emit(VehicleDetailContract.VehicleDetailEffect.ShowToast("삭제 중 오류가 발생했습니다: ${e.message}"))
            }
        }
    }
}

