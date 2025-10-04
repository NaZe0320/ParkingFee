package com.naze.parkingfee.presentation.ui.screens.settings.vehicles.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.parkingfee.domain.usecase.vehicle.DeleteVehicleUseCase
import com.naze.parkingfee.domain.usecase.vehicle.GetVehiclesUseCase
import com.naze.parkingfee.domain.usecase.GetSelectedVehicleIdUseCase
import com.naze.parkingfee.domain.usecase.SetSelectedVehicleIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 차량 목록 화면의 ViewModel
 */
@HiltViewModel
class VehicleListViewModel @Inject constructor(
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val deleteVehicleUseCase: DeleteVehicleUseCase,
    private val getSelectedVehicleIdUseCase: GetSelectedVehicleIdUseCase,
    private val setSelectedVehicleIdUseCase: SetSelectedVehicleIdUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(VehicleListContract.VehicleListState())
    val state: StateFlow<VehicleListContract.VehicleListState> = _state.asStateFlow()
    
    private val _effect = MutableSharedFlow<VehicleListContract.VehicleListEffect>()
    val effect: SharedFlow<VehicleListContract.VehicleListEffect> = _effect.asSharedFlow()
    
    init {
        loadVehicles()
        observeSelectedVehicleId()
    }
    
    /**
     * 화면이 다시 포커스될 때 호출 (차량 추가/편집 후 돌아올 때)
     */
    fun refreshVehicles() {
        loadVehicles()
    }
    
    fun processIntent(intent: VehicleListContract.VehicleListIntent) {
        when (intent) {
            is VehicleListContract.VehicleListIntent.LoadVehicles -> loadVehicles()
            is VehicleListContract.VehicleListIntent.NavigateToAddVehicle -> navigateToAddVehicle()
            is VehicleListContract.VehicleListIntent.DeleteVehicle -> deleteVehicle(intent.vehicleId)
            is VehicleListContract.VehicleListIntent.NavigateToEditVehicle -> navigateToEditVehicle(intent.vehicleId)
            is VehicleListContract.VehicleListIntent.SelectVehicle -> selectVehicle(intent.vehicleId)
            is VehicleListContract.VehicleListIntent.NavigateBack -> navigateBack()
        }
    }
    
    private fun loadVehicles() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val vehicles = getVehiclesUseCase()
                _state.update { 
                    it.copy(
                        isLoading = false,
                        vehicles = vehicles,
                        canAddVehicle = vehicles.size < 3
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "차량 목록을 불러오는데 실패했습니다: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun navigateToAddVehicle() {
        viewModelScope.launch {
            _effect.emit(VehicleListContract.VehicleListEffect.NavigateToAddVehicle)
        }
    }
    
    private fun navigateToEditVehicle(vehicleId: String) {
        viewModelScope.launch {
            _effect.emit(VehicleListContract.VehicleListEffect.NavigateToEditVehicle(vehicleId))
        }
    }
    
    private fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            try {
                val vehicle = _state.value.vehicles.find { it.id == vehicleId }
                if (vehicle != null) {
                    _effect.emit(
                        VehicleListContract.VehicleListEffect.ShowDeleteConfirmation(
                            vehicleId = vehicleId,
                            vehicleName = vehicle.displayName
                        )
                    )
                }
            } catch (e: Exception) {
                _effect.emit(VehicleListContract.VehicleListEffect.ShowToast("차량 삭제 확인 중 오류가 발생했습니다."))
            }
        }
    }
    
    fun confirmDeleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            try {
                val result = deleteVehicleUseCase(vehicleId)
                if (result.isSuccess) {
                    _effect.emit(VehicleListContract.VehicleListEffect.ShowToast("차량이 삭제되었습니다."))
                    loadVehicles() // 목록 새로고침
                } else {
                    _effect.emit(VehicleListContract.VehicleListEffect.ShowToast("차량 삭제에 실패했습니다."))
                }
            } catch (e: Exception) {
                _effect.emit(VehicleListContract.VehicleListEffect.ShowToast("차량 삭제 중 오류가 발생했습니다: ${e.message}"))
            }
        }
    }
    
    private fun navigateBack() {
        viewModelScope.launch {
            _effect.emit(VehicleListContract.VehicleListEffect.NavigateBack)
        }
    }
    
    /**
     * 선택된 차량 ID를 관찰합니다.
     */
    private fun observeSelectedVehicleId() {
        viewModelScope.launch {
            getSelectedVehicleIdUseCase.execute().collect { selectedVehicleId ->
                _state.update { it.copy(selectedVehicleId = selectedVehicleId) }
            }
        }
    }
    
    /**
     * 차량을 선택합니다.
     */
    private fun selectVehicle(vehicleId: String) {
        viewModelScope.launch {
            try {
                setSelectedVehicleIdUseCase.execute(vehicleId)
                _effect.emit(VehicleListContract.VehicleListEffect.ShowToast("차량이 선택되었습니다."))
            } catch (e: Exception) {
                _effect.emit(VehicleListContract.VehicleListEffect.ShowToast("차량 선택 중 오류가 발생했습니다."))
            }
        }
    }
}
