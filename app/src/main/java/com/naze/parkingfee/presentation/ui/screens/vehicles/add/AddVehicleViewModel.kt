package com.naze.parkingfee.presentation.ui.screens.settings.vehicles.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.parkingfee.domain.common.discount.DiscountEligibility
import com.naze.parkingfee.domain.common.discount.VehicleDiscountEligibilities
import com.naze.parkingfee.domain.model.vehicle.Vehicle
import com.naze.parkingfee.domain.repository.VehicleRepository
import com.naze.parkingfee.domain.usecase.selectedvehicle.GetSelectedVehicleIdUseCase
import com.naze.parkingfee.domain.usecase.selectedvehicle.SetSelectedVehicleIdUseCase
import com.naze.parkingfee.domain.usecase.vehicle.AddVehicleUseCase
import com.naze.parkingfee.domain.usecase.vehicle.UpdateVehicleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * 차량 등록 화면의 ViewModel
 */
@HiltViewModel
class AddVehicleViewModel @Inject constructor(
    private val addVehicleUseCase: AddVehicleUseCase,
    private val updateVehicleUseCase: UpdateVehicleUseCase,
    private val vehicleRepository: VehicleRepository,
    private val getSelectedVehicleIdUseCase: GetSelectedVehicleIdUseCase,
    private val setSelectedVehicleIdUseCase: SetSelectedVehicleIdUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(AddVehicleContract.AddVehicleState())
    val state: StateFlow<AddVehicleContract.AddVehicleState> = _state.asStateFlow()
    
    private val _effect = MutableSharedFlow<AddVehicleContract.AddVehicleEffect>()
    val effect: SharedFlow<AddVehicleContract.AddVehicleEffect> = _effect.asSharedFlow()
    
    fun processIntent(intent: AddVehicleContract.AddVehicleIntent) {
        when (intent) {
            is AddVehicleContract.AddVehicleIntent.Initialize -> initialize()
            is AddVehicleContract.AddVehicleIntent.UpdateVehicleName -> updateVehicleName(intent.name)
            is AddVehicleContract.AddVehicleIntent.UpdatePlateNumber -> updatePlateNumber(intent.plateNumber)
            is AddVehicleContract.AddVehicleIntent.ToggleCompactCarDiscount -> toggleCompactCarDiscount(intent.enabled)
            is AddVehicleContract.AddVehicleIntent.ToggleNationalMeritDiscount -> toggleNationalMeritDiscount(intent.enabled)
            is AddVehicleContract.AddVehicleIntent.ToggleDisabledDiscount -> toggleDisabledDiscount(intent.enabled)
            is AddVehicleContract.AddVehicleIntent.OpenOcrScreen -> openOcrScreen()
            is AddVehicleContract.AddVehicleIntent.SaveVehicle -> saveVehicle()
            is AddVehicleContract.AddVehicleIntent.NavigateBack -> navigateBack()
            is AddVehicleContract.AddVehicleIntent.LoadVehicleForEdit -> loadVehicleForEdit(intent.vehicleId)
        }
    }
    
    private fun initialize() {
        _state.update { it.copy(isLoading = false) }
    }
    
    private fun updateVehicleName(name: String) {
        _state.update { it.copy(vehicleName = name) }
    }
    
    private fun updatePlateNumber(plateNumber: String) {
        _state.update { it.copy(plateNumber = plateNumber) }
    }
    
    private fun toggleCompactCarDiscount(enabled: Boolean) {
        _state.update { it.copy(compactCarDiscount = enabled) }
    }
    
    private fun toggleNationalMeritDiscount(enabled: Boolean) {
        _state.update { it.copy(nationalMeritDiscount = enabled) }
    }
    
    private fun toggleDisabledDiscount(enabled: Boolean) {
        _state.update { it.copy(disabledDiscount = enabled) }
    }
    
    private fun openOcrScreen() {
        viewModelScope.launch {
            _effect.emit(AddVehicleContract.AddVehicleEffect.OpenOcrScreen)
        }
    }
    
    private fun saveVehicle() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            
            try {
                // 유효성 검사
                val validationErrors = validateInput()
                if (validationErrors.isNotEmpty()) {
                    _state.update { 
                        it.copy(
                            isSaving = false,
                            validationErrors = validationErrors
                        )
                    }
                    return@launch
                }
                
                val vehicle = createVehicleFromState()
                
                val result = if (_state.value.isEditMode) {
                    updateVehicleUseCase.execute(vehicle)
                } else {
                    addVehicleUseCase.execute(vehicle)
                }
                
                if (result.isSuccess) {
                    // 차량 등록 직후, 아직 선택된 차량이 없다면 방금 등록한 차량을 선택
                    if (!_state.value.isEditMode) {
                        val currentSelected = getSelectedVehicleIdUseCase.execute().first()
                        if (currentSelected == null) {
                            setSelectedVehicleIdUseCase.execute(vehicle.id)
                        }
                    }
                    _effect.emit(AddVehicleContract.AddVehicleEffect.ShowToast(
                        if (_state.value.isEditMode) "차량이 수정되었습니다." else "차량이 등록되었습니다."
                    ))
                    _effect.emit(AddVehicleContract.AddVehicleEffect.NavigateBack)
                } else {
                    _state.update { 
                        it.copy(
                            isSaving = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "저장에 실패했습니다."
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isSaving = false,
                        errorMessage = "저장 중 오류가 발생했습니다: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun navigateBack() {
        viewModelScope.launch {
            _effect.emit(AddVehicleContract.AddVehicleEffect.NavigateBack)
        }
    }
    
    private fun loadVehicleForEdit(vehicleId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val vehicle = vehicleRepository.getVehicleById(vehicleId)
                if (vehicle != null) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            isEditMode = true,
                            vehicleId = vehicle.id,
                            vehicleName = vehicle.name ?: "",
                            plateNumber = vehicle.plateNumber ?: "",
                            compactCarDiscount = vehicle.discountEligibilities.compactCar.enabled,
                            nationalMeritDiscount = vehicle.discountEligibilities.nationalMerit.enabled,
                            disabledDiscount = vehicle.discountEligibilities.disabled.enabled
                        )
                    }
                } else {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "차량 정보를 찾을 수 없습니다."
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "차량 정보를 불러오는데 실패했습니다: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun validateInput(): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        val currentState = _state.value
        
        // 차량 이름 검사 (입력된 경우 20자 제한)
        if (currentState.vehicleName.isNotBlank() && currentState.vehicleName.length > 20) {
            errors["vehicleName"] = "차량 이름은 20자 이하여야 합니다."
        }
        
        // 번호판 검증 (선택사항이지만 입력된 경우 형식 검증)
        // 형식: 숫자(2~3자리) + 한글 완성형(1자리) + 숫자(4자리)
        if (currentState.plateNumber.isNotBlank()) {
            val plateNumber = currentState.plateNumber
            if (!isValidPlateNumberFormat(plateNumber)) {
                errors["plateNumber"] = "번호판 형식이 올바르지 않습니다 (예: 12가3456, 123가3456)"
            }
        }
        
        return errors
    }
    
    /**
     * 번호판 형식 검증
     * 형식: 숫자(2~3자리) + 한글 완성형(1자리) + 숫자(4자리)
     * 예: "12가3456", "123가3456"
     */
    private fun isValidPlateNumberFormat(plateNumber: String): Boolean {
        if (plateNumber.length < 7 || plateNumber.length > 8) {
            return false
        }
        
        // 정규식 패턴: 숫자 2~3자리 + 한글 완성형 1자리 + 숫자 4자리
        val pattern = Regex("^\\d{2,3}[가-힣]\\d{4}$")
        return pattern.matches(plateNumber)
    }
    
    private suspend fun createVehicleFromState(): Vehicle {
        val currentState = _state.value
        
        // 차량명이 비어있으면 기본 이름 생성
        val vehicleName = if (currentState.vehicleName.isNotBlank()) {
            currentState.vehicleName
        } else {
            val vehicleCount = vehicleRepository.getVehicleCount()
            "자동차${vehicleCount + 1}"
        }
        
        // 편집 모드인 경우 기존 차량 정보를 가져와서 createdAt 유지
        val existingVehicle = if (currentState.isEditMode && currentState.vehicleId != null) {
            vehicleRepository.getVehicleById(currentState.vehicleId)
        } else null
        
        return Vehicle(
            id = currentState.vehicleId ?: UUID.randomUUID().toString(),
            name = vehicleName,
            plateNumber = currentState.plateNumber.takeIf { it.isNotBlank() },
            discountEligibilities = VehicleDiscountEligibilities(
                compactCar = DiscountEligibility.CompactCar(currentState.compactCarDiscount),
                nationalMerit = DiscountEligibility.NationalMerit(currentState.nationalMeritDiscount),
                disabled = DiscountEligibility.Disabled(currentState.disabledDiscount)
            ),
            createdAt = existingVehicle?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}
