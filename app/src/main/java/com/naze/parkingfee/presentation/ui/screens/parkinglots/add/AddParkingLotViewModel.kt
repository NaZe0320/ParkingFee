package com.naze.parkingfee.presentation.ui.screens.parkinglots.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.domain.model.FeeStructure
import com.naze.parkingfee.domain.model.BasicFeeRule
import com.naze.parkingfee.domain.model.AdditionalFeeRule
import com.naze.parkingfee.domain.model.DailyMaxFeeRule
import com.naze.parkingfee.domain.model.CustomFeeRule
import com.naze.parkingfee.domain.usecase.AddParkingZoneUseCase
import com.naze.parkingfee.domain.usecase.GetParkingZonesUseCase
import com.naze.parkingfee.domain.usecase.GetParkingZoneByIdUseCase
import com.naze.parkingfee.domain.usecase.UpdateParkingZoneUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * 주차장 추가 화면의 ViewModel
 * MVI 패턴에 따라 Intent를 처리하고 State를 관리합니다.
 */
@HiltViewModel
class AddParkingLotViewModel @Inject constructor(
    private val addParkingZoneUseCase: AddParkingZoneUseCase,
    private val getParkingZonesUseCase: GetParkingZonesUseCase,
    private val getParkingZoneByIdUseCase: GetParkingZoneByIdUseCase,
    private val updateParkingZoneUseCase: UpdateParkingZoneUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddParkingLotContract.AddParkingLotState())
    val state: StateFlow<AddParkingLotContract.AddParkingLotState> = _state.asStateFlow()

    private val _effect = MutableStateFlow<AddParkingLotContract.AddParkingLotEffect?>(null)
    val effect: StateFlow<AddParkingLotContract.AddParkingLotEffect?> = _effect.asStateFlow()

    /**
     * Intent를 처리하는 메서드
     */
    fun processIntent(intent: AddParkingLotContract.AddParkingLotIntent) {
        when (intent) {
            is AddParkingLotContract.AddParkingLotIntent.Initialize -> initialize()
            is AddParkingLotContract.AddParkingLotIntent.LoadZoneForEdit -> loadZoneForEdit(intent.zoneId)
            is AddParkingLotContract.AddParkingLotIntent.OpenOcrScreen -> openOcrScreen()
            is AddParkingLotContract.AddParkingLotIntent.UpdateParkingLotName -> updateParkingLotName(intent.name)
            is AddParkingLotContract.AddParkingLotIntent.ToggleUseDefaultName -> toggleUseDefaultName(intent.useDefault)
            is AddParkingLotContract.AddParkingLotIntent.ToggleIsPublic -> toggleIsPublic(intent.isPublic)
            is AddParkingLotContract.AddParkingLotIntent.UpdateBasicFeeDuration -> updateBasicFeeDuration(intent.minutes)
            is AddParkingLotContract.AddParkingLotIntent.UpdateBasicFeeAmount -> updateBasicFeeAmount(intent.amount)
            is AddParkingLotContract.AddParkingLotIntent.UpdateAdditionalFeeInterval -> updateAdditionalFeeInterval(intent.minutes)
            is AddParkingLotContract.AddParkingLotIntent.UpdateAdditionalFeeAmount -> updateAdditionalFeeAmount(intent.amount)
            is AddParkingLotContract.AddParkingLotIntent.ToggleDailyMaxFee -> toggleDailyMaxFee(intent.enabled)
            is AddParkingLotContract.AddParkingLotIntent.UpdateDailyMaxFeeAmount -> updateDailyMaxFeeAmount(intent.amount)
            is AddParkingLotContract.AddParkingLotIntent.AddCustomFeeRule -> addCustomFeeRule()
            is AddParkingLotContract.AddParkingLotIntent.RemoveCustomFeeRule -> removeCustomFeeRule(intent.index)
            is AddParkingLotContract.AddParkingLotIntent.UpdateCustomFeeRule -> updateCustomFeeRule(intent.index, intent.minMinutes, intent.maxMinutes, intent.fee)
            is AddParkingLotContract.AddParkingLotIntent.SaveParkingLot -> saveParkingLot()
            is AddParkingLotContract.AddParkingLotIntent.NavigateBack -> navigateBack()
            is AddParkingLotContract.AddParkingLotIntent.ResetForm -> resetForm()
        }
    }

    /**
     * OCR 화면 열기
     */
    private fun openOcrScreen() {
        _effect.value = AddParkingLotContract.AddParkingLotEffect.OpenOcrScreen
    }

    /**
     * 주차장 이름 업데이트
     */
    private fun updateParkingLotName(name: String) {
        _state.update { it.copy(parkingLotName = name) }
    }

    /**
     * 기본 이름 사용 여부 토글
     */
    private fun toggleUseDefaultName(useDefault: Boolean) {
        _state.update { 
            it.copy(
                useDefaultName = useDefault,
                parkingLotName = if (useDefault) "" else it.parkingLotName
            )
        }
    }

    /**
     * 공영 주차장 여부 토글
     */
    private fun toggleIsPublic(isPublic: Boolean) {
        _state.update { it.copy(isPublic = isPublic) }
    }

    /**
     * 기본 요금 시간 업데이트
     */
    private fun updateBasicFeeDuration(minutes: Int) {
        if (minutes > 0) {
            _state.update { it.copy(basicFeeDuration = minutes) }
        }
    }

    /**
     * 기본 요금 금액 업데이트
     */
    private fun updateBasicFeeAmount(amount: Int) {
        if (amount >= 0) {
            _state.update { it.copy(basicFeeAmount = amount) }
        }
    }

    /**
     * 추가 요금 간격 업데이트
     */
    private fun updateAdditionalFeeInterval(minutes: Int) {
        if (minutes > 0) {
            _state.update { it.copy(additionalFeeInterval = minutes) }
        }
    }

    /**
     * 추가 요금 금액 업데이트
     */
    private fun updateAdditionalFeeAmount(amount: Int) {
        if (amount >= 0) {
            _state.update { it.copy(additionalFeeAmount = amount) }
        }
    }

    /**
     * 일 최대 요금 활성화/비활성화
     */
    private fun toggleDailyMaxFee(enabled: Boolean) {
        _state.update { it.copy(dailyMaxFeeEnabled = enabled) }
    }

    /**
     * 일 최대 요금 금액 업데이트
     */
    private fun updateDailyMaxFeeAmount(amount: Int) {
        if (amount >= 0) {
            _state.update { it.copy(dailyMaxFeeAmount = amount) }
        }
    }

    /**
     * 커스텀 요금 구간 추가
     */
    private fun addCustomFeeRule() {
        val currentRules = _state.value.customFeeRules.toMutableList()
        val newRule = CustomFeeRule(
            minMinutes = 0,
            maxMinutes = null,
            fee = 0
        )
        currentRules.add(newRule)
        _state.update { it.copy(customFeeRules = currentRules) }
    }

    /**
     * 커스텀 요금 구간 제거
     */
    private fun removeCustomFeeRule(index: Int) {
        val currentRules = _state.value.customFeeRules.toMutableList()
        if (index in currentRules.indices) {
            currentRules.removeAt(index)
            _state.update { it.copy(customFeeRules = currentRules) }
        }
    }

    /**
     * 커스텀 요금 구간 업데이트
     */
    private fun updateCustomFeeRule(index: Int, minMinutes: Int, maxMinutes: Int?, fee: Int) {
        val currentRules = _state.value.customFeeRules.toMutableList()
        if (index in currentRules.indices) {
            currentRules[index] = CustomFeeRule(
                minMinutes = minMinutes,
                maxMinutes = maxMinutes,
                fee = fee
            )
            _state.update { it.copy(customFeeRules = currentRules) }
        }
    }

    /**
     * 주차 구역 저장
     */
    private fun saveParkingLot() {
        val currentState = _state.value
        
        // 재진입 방지 가드
        if (currentState.isSaving) {
            return
        }
        
        // 유효성 검사
        val validationErrors = validateForm(currentState)
        if (validationErrors.isNotEmpty()) {
            _state.update { it.copy(validationErrors = validationErrors) }
            return
        }

        // 저장 시작 즉시 버튼 비활성화
        _state.update { it.copy(isSaving = true, validationErrors = emptyMap()) }

        viewModelScope.launch {
            try {

                if (currentState.isEditMode && currentState.editingZoneId != null) {
                    // 편집 모드: 기존 구역 업데이트
                    val existingZone = getParkingZoneByIdUseCase.execute(currentState.editingZoneId!!)
                    if (existingZone != null) {
                        val updatedZone = existingZone.copy(
                            name = if (currentState.useDefaultName) existingZone.name else currentState.parkingLotName,
                            hourlyRate = calculateHourlyRate(currentState),
                            isPublic = currentState.isPublic,
                            feeStructure = createFeeStructure(currentState),
                            updatedAt = System.currentTimeMillis()
                        )
                        updateParkingZoneUseCase.execute(updatedZone)
                    } else {
                        throw Exception("편집할 구역을 찾을 수 없습니다.")
                    }
                } else {
                    // 추가 모드: 새 구역 생성
                    val existingZones = getParkingZonesUseCase.execute()
                    val nextSequenceNumber = existingZones.size + 1

                    val parkingZone = ParkingZone(
                        id = UUID.randomUUID().toString(),
                        name = if (currentState.useDefaultName) "주차장$nextSequenceNumber" else currentState.parkingLotName,
                        hourlyRate = calculateHourlyRate(currentState),
                        maxCapacity = 100,
                        currentOccupancy = 0,
                        isPublic = currentState.isPublic,
                        feeStructure = createFeeStructure(currentState)
                    )

                    addParkingZoneUseCase.execute(parkingZone)
                }

                _state.update { 
                    it.copy(
                        isSaving = false,
                        isSaved = true
                    )
                }

                _effect.value = AddParkingLotContract.AddParkingLotEffect.ShowToast(
                    if (currentState.isEditMode) "주차장이 성공적으로 수정되었습니다." else "주차장이 성공적으로 추가되었습니다."
                )
                
                // 잠시 후 뒤로가기
                kotlinx.coroutines.delay(1500)
                _effect.value = AddParkingLotContract.AddParkingLotEffect.NavigateBack

            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "주차장 저장 중 오류가 발생했습니다."
                    )
                }
                _effect.value = AddParkingLotContract.AddParkingLotEffect.ShowToast("주차장 저장에 실패했습니다.")
            }
        }
    }

    /**
     * 폼 유효성 검사
     */
    private fun validateForm(state: AddParkingLotContract.AddParkingLotState): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        // 주차장 이름 검사 (기본 이름 사용하지 않는 경우)
        if (!state.useDefaultName && state.parkingLotName.isBlank()) {
            errors["parkingLotName"] = "주차장 이름을 입력해주세요."
        }

        // 기본 요금 검사
        if (state.basicFeeDuration <= 0) {
            errors["basicFeeDuration"] = "기본 요금 시간은 0보다 커야 합니다."
        }
        if (state.basicFeeAmount < 0) {
            errors["basicFeeAmount"] = "기본 요금은 0원 이상이어야 합니다."
        }

        // 추가 요금 검사
        if (state.additionalFeeInterval <= 0) {
            errors["additionalFeeInterval"] = "추가 요금 간격은 0보다 커야 합니다."
        }
        if (state.additionalFeeAmount < 0) {
            errors["additionalFeeAmount"] = "추가 요금은 0원 이상이어야 합니다."
        }

        // 일 최대 요금 검사
        if (state.dailyMaxFeeEnabled && state.dailyMaxFeeAmount < 0) {
            errors["dailyMaxFeeAmount"] = "일 최대 요금은 0원 이상이어야 합니다."
        }

        // 커스텀 요금 구간 검사
        state.customFeeRules.forEachIndexed { index, rule ->
            if (rule.minMinutes < 0) {
                errors["customFeeRule_${index}_minMinutes"] = "최소 시간은 0분 이상이어야 합니다."
            }
            if (rule.maxMinutes != null && rule.maxMinutes <= rule.minMinutes) {
                errors["customFeeRule_${index}_maxMinutes"] = "최대 시간은 최소 시간보다 커야 합니다."
            }
            if (rule.fee < 0) {
                errors["customFeeRule_${index}_fee"] = "요금은 0원 이상이어야 합니다."
            }
        }

        return errors
    }

    /**
     * 요금 체계 생성
     */
    private fun createFeeStructure(state: AddParkingLotContract.AddParkingLotState): FeeStructure {
        return FeeStructure(
            basicFee = BasicFeeRule(
                durationMinutes = state.basicFeeDuration,
                fee = state.basicFeeAmount
            ),
            additionalFee = AdditionalFeeRule(
                intervalMinutes = state.additionalFeeInterval,
                fee = state.additionalFeeAmount
            ),
            dailyMaxFee = if (state.dailyMaxFeeEnabled) {
                DailyMaxFeeRule(maxFee = state.dailyMaxFeeAmount)
            } else null,
            customFeeRules = state.customFeeRules.filter { it.fee > 0 }
        )
    }

    /**
     * 뒤로가기
     */
    private fun navigateBack() {
        _effect.value = AddParkingLotContract.AddParkingLotEffect.NavigateBack
    }

    /**
     * 기본 시간당 요금 계산 (복잡한 요금 체계의 대략적인 시간당 요금)
     */
    private fun calculateHourlyRate(state: AddParkingLotContract.AddParkingLotState): Double {
        val basicFee = state.basicFeeAmount
        val basicDuration = state.basicFeeDuration
        val additionalFee = state.additionalFeeAmount
        val additionalInterval = state.additionalFeeInterval
        
        // 기본 요금 + 추가 요금을 고려한 대략적인 시간당 요금 계산
        val basicHourlyRate = (basicFee.toDouble() / basicDuration) * 60
        val additionalHourlyRate = (additionalFee.toDouble() / additionalInterval) * 60
        
        return basicHourlyRate + additionalHourlyRate
    }

    /**
     * 폼 초기화
     */
    private fun resetForm() {
        _state.value = AddParkingLotContract.AddParkingLotState()
    }

    /**
     * 초기화
     */
    private fun initialize() {
        // 기본 상태로 초기화
        _state.value = AddParkingLotContract.AddParkingLotState()
    }

    /**
     * 편집을 위한 구역 로드
     */
    private fun loadZoneForEdit(zoneId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, errorMessage = null) }
                
                val zone = getParkingZoneByIdUseCase.execute(zoneId)
                
                if (zone != null) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            isEditMode = true,
                            editingZoneId = zoneId,
                            parkingLotName = zone.name,
                            useDefaultName = false,
                            isPublic = zone.isPublic,
                            basicFeeDuration = zone.feeStructure?.basicFee?.durationMinutes ?: 30,
                            basicFeeAmount = zone.feeStructure?.basicFee?.fee ?: 1000,
                            additionalFeeInterval = zone.feeStructure?.additionalFee?.intervalMinutes ?: 10,
                            additionalFeeAmount = zone.feeStructure?.additionalFee?.fee ?: 500,
                            dailyMaxFeeEnabled = zone.feeStructure?.dailyMaxFee != null,
                            dailyMaxFeeAmount = zone.feeStructure?.dailyMaxFee?.maxFee ?: 10000,
                            customFeeRules = zone.feeStructure?.customFeeRules ?: emptyList()
                        )
                    }
                } else {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "구역을 찾을 수 없습니다."
                        )
                    }
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
}
