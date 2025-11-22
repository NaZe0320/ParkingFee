package com.naze.parkingfee.presentation.ui.screens.parkinglots.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.domain.model.FeeStructure
import com.naze.parkingfee.domain.model.BasicFeeRule
import com.naze.parkingfee.domain.model.AdditionalFeeRule
import com.naze.parkingfee.domain.model.DailyMaxFeeRule
import com.naze.parkingfee.domain.model.CustomFeeRule
import com.naze.parkingfee.domain.usecase.parkingzone.AddParkingZoneUseCase
import com.naze.parkingfee.domain.usecase.parkingzone.GetParkingZoneByIdUseCase
import com.naze.parkingfee.domain.usecase.parkingzone.UpdateParkingZoneUseCase
import com.naze.parkingfee.domain.repository.ParkingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val getParkingZoneByIdUseCase: GetParkingZoneByIdUseCase,
    private val updateParkingZoneUseCase: UpdateParkingZoneUseCase,
    private val parkingRepository: ParkingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddParkingLotContract.AddParkingLotState())
    val state: StateFlow<AddParkingLotContract.AddParkingLotState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AddParkingLotContract.AddParkingLotEffect>()
    val effect: SharedFlow<AddParkingLotContract.AddParkingLotEffect> = _effect.asSharedFlow()

    /**
     * Intent를 처리하는 메서드
     */
    fun processIntent(intent: AddParkingLotContract.AddParkingLotIntent) {
        when (intent) {
            is AddParkingLotContract.AddParkingLotIntent.Initialize -> initialize()
            is AddParkingLotContract.AddParkingLotIntent.LoadZoneForEdit -> loadZoneForEdit(intent.zoneId)
            is AddParkingLotContract.AddParkingLotIntent.OpenOcrScreen -> openOcrScreen()
            is AddParkingLotContract.AddParkingLotIntent.ApplyOcrResult -> applyOcrResult()
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
            
            // 고급 모드 관련 처리
            is AddParkingLotContract.AddParkingLotIntent.ToggleAdvancedMode -> toggleAdvancedMode(intent.enabled)
            is AddParkingLotContract.AddParkingLotIntent.AddFeeRow -> addFeeRow()
            is AddParkingLotContract.AddParkingLotIntent.RemoveFeeRow -> removeFeeRow(intent.index)
            is AddParkingLotContract.AddParkingLotIntent.UpdateFeeRow -> updateFeeRow(
                intent.index, 
                intent.startTime, 
                intent.endTime, 
                intent.unitMinutes, 
                intent.unitFee,
                intent.isFixedFee
            )
        }
    }

    /**
     * OCR 화면 열기
     */
    private fun openOcrScreen() {
        viewModelScope.launch {
            _effect.emit(AddParkingLotContract.AddParkingLotEffect.OpenOcrScreen)
        }
    }

    /**
     * OCR 결과 적용
     */
    private fun applyOcrResult() {
        val ocrResult = com.naze.parkingfee.presentation.ui.screens.ocr.OcrResultManager.getResult()
        
        if (ocrResult != null) {
            _state.update {
                it.copy(
                    // 주차장 이름 적용
                    parkingLotName = ocrResult.parkingLotName ?: it.parkingLotName,
                    // 공영 여부 적용
                    isPublic = ocrResult.isPublic,
                    // 고급 모드 활성화
                    isAdvancedMode = true,
                    // 요금 구간 적용
                    advancedFeeRows = ocrResult.feeRows,
                    // 일 최대 요금 적용
                    dailyMaxFeeEnabled = ocrResult.dailyMaxFee != null,
                    dailyMaxFeeAmount = ocrResult.dailyMaxFee ?: it.dailyMaxFeeAmount
                )
            }
            
            // 사용 완료 후 결과 정리
            com.naze.parkingfee.presentation.ui.screens.ocr.OcrResultManager.clearResult()
            
            viewModelScope.launch {
                _effect.emit(
                    AddParkingLotContract.AddParkingLotEffect.ShowToast(
                        "OCR 결과가 적용되었습니다. 내용을 확인해주세요."
                    )
                )
            }
        }
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
        if (minutes >= 0) {
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
        if (minutes >= 0) {
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
            unitMinutes = 10, // 기본값
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
            val existingRule = currentRules[index]
            currentRules[index] = CustomFeeRule(
                minMinutes = minMinutes,
                maxMinutes = maxMinutes,
                unitMinutes = existingRule.unitMinutes, // 기존 값 유지
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
                    val existingZone = getParkingZoneByIdUseCase.execute(currentState.editingZoneId)
                    if (existingZone != null) {
                        val updatedZone = existingZone.copy(
                            name = if (currentState.parkingLotName.isBlank()) existingZone.name else currentState.parkingLotName,
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
                    // 주차장 이름이 비어있으면 기본 이름 생성
                    val parkingLotName = if (currentState.parkingLotName.isNotBlank()) {
                        currentState.parkingLotName
                    } else {
                        val zoneCount = parkingRepository.getParkingZoneCount()
                        "주차장${zoneCount + 1}"
                    }
                    
                    val parkingZone = ParkingZone(
                        id = UUID.randomUUID().toString(),
                        name = parkingLotName,
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

                // Toast 메시지와 함께 즉시 뒤로가기 (Flow가 자동으로 목록을 업데이트)
                _effect.emit(AddParkingLotContract.AddParkingLotEffect.ShowToast(
                    if (currentState.isEditMode) "주차장이 성공적으로 수정되었습니다." else "주차장이 성공적으로 추가되었습니다."
                ))
                _effect.emit(AddParkingLotContract.AddParkingLotEffect.NavigateBack)

            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "주차장 저장 중 오류가 발생했습니다."
                    )
                }
                _effect.emit(AddParkingLotContract.AddParkingLotEffect.ShowToast("주차장 저장에 실패했습니다."))
            }
        }
    }

    /**
     * 폼 유효성 검사
     */
    private fun validateForm(state: AddParkingLotContract.AddParkingLotState): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        // 주차장 이름 검사 (입력된 경우 20자 제한)
        if (state.parkingLotName.isNotBlank() && state.parkingLotName.length > 20) {
            errors["parkingLotName"] = "주차장 이름은 20자 이하여야 합니다."
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
            if (rule.unitMinutes <= 0) {
                errors["customFeeRule_${index}_unitMinutes"] = "단위 시간은 0보다 커야 합니다."
            }
            if (rule.fee < 0) {
                errors["customFeeRule_${index}_fee"] = "요금은 0원 이상이어야 합니다."
            }
        }

        // 고급 모드 요금 구간 검사
        if (state.isAdvancedMode) {
            state.advancedFeeRows.forEachIndexed { index, row ->
                if (row.startTime < 0) {
                    errors["advancedFeeRow_${index}_startTime"] = "시작 시간은 0분 이상이어야 합니다."
                }
                if (row.endTime != null && row.endTime <= row.startTime) {
                    errors["advancedFeeRow_${index}_endTime"] = "종료 시간은 시작 시간보다 커야 합니다."
                }
                if (row.unitMinutes <= 0) {
                    errors["advancedFeeRow_${index}_unitMinutes"] = "단위 시간은 0보다 커야 합니다."
                }
                if (row.unitFee < 0) {
                    errors["advancedFeeRow_${index}_unitFee"] = "요금은 0원 이상이어야 합니다."
                }
            }
        }

        return errors
    }

    /**
     * 요금 체계 생성
     */
    private fun createFeeStructure(state: AddParkingLotContract.AddParkingLotState): FeeStructure {
        // 고급 모드인 경우
        if (state.isAdvancedMode && state.advancedFeeRows.isNotEmpty()) {
            val rows = state.advancedFeeRows
            
            // 첫 번째 행을 BasicFeeRule로 사용
            val basicFee = if (rows.isNotEmpty()) {
                BasicFeeRule(
                    durationMinutes = rows[0].endTime ?: rows[0].startTime + 30,
                    fee = rows[0].unitFee
                )
            } else {
                BasicFeeRule(durationMinutes = 30, fee = 1000)
            }
            
            // 두 번째 행을 AdditionalFeeRule로 사용
            val additionalFee = if (rows.size > 1) {
                AdditionalFeeRule(
                    intervalMinutes = rows[1].unitMinutes,
                    fee = rows[1].unitFee
                )
            } else {
                AdditionalFeeRule(intervalMinutes = 10, fee = 500)
            }
            
            // 모든 행을 CustomFeeRule로 변환하여 저장
            val customFeeRules = convertFeeRowsToCustomFeeRules(rows)
            
            return FeeStructure(
                basicFee = basicFee,
                additionalFee = additionalFee,
                dailyMaxFee = if (state.dailyMaxFeeEnabled) {
                    DailyMaxFeeRule(maxFee = state.dailyMaxFeeAmount)
                } else null,
                customFeeRules = customFeeRules
            )
        }
        
        // 단순 모드인 경우 (기존 로직)
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
        viewModelScope.launch {
            _effect.emit(AddParkingLotContract.AddParkingLotEffect.NavigateBack)
        }
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
    
    // ==================== 고급 모드 관련 메서드 ====================
    
    /**
     * FeeRow 리스트를 CustomFeeRule 리스트로 변환
     */
    private fun convertFeeRowsToCustomFeeRules(rows: List<AddParkingLotContract.FeeRow>): List<CustomFeeRule> {
        return rows.map { row ->
            CustomFeeRule(
                minMinutes = row.startTime,
                maxMinutes = row.endTime,
                unitMinutes = row.unitMinutes,
                fee = row.unitFee,
                isFixedFee = row.isFixedFee
            )
        }
    }
    
    /**
     * CustomFeeRule 리스트를 FeeRow 리스트로 변환
     */
    private fun convertCustomFeeRulesToFeeRows(rules: List<CustomFeeRule>): List<AddParkingLotContract.FeeRow> {
        return rules.map { rule ->
            AddParkingLotContract.FeeRow(
                startTime = rule.minMinutes,
                endTime = rule.maxMinutes,
                unitMinutes = rule.unitMinutes,
                unitFee = rule.fee,
                isFixedFee = rule.isFixedFee
            )
        }
    }
    
    /**
     * 고급 모드 토글
     */
    private fun toggleAdvancedMode(enabled: Boolean) {
        if (enabled) {
            // Simple -> Advanced 변환
            val currentState = _state.value
            
            val rows = mutableListOf<AddParkingLotContract.FeeRow>()
            
            // Row 1: 최초 구간 (0 ~ 기본시간)
            rows.add(
                AddParkingLotContract.FeeRow(
                    startTime = 0,
                    endTime = currentState.basicFeeDuration,
                    unitMinutes = currentState.basicFeeDuration,
                    unitFee = currentState.basicFeeAmount
                )
            )
            
            // Row 2: 이후 구간 (기본시간 ~ 무제한)
            rows.add(
                AddParkingLotContract.FeeRow(
                    startTime = currentState.basicFeeDuration,
                    endTime = null, // 무제한
                    unitMinutes = currentState.additionalFeeInterval,
                    unitFee = currentState.additionalFeeAmount
                )
            )
            
            _state.update { it.copy(isAdvancedMode = true, advancedFeeRows = rows) }
            
        } else {
            // Advanced -> Simple 변환 (초기화)
            _state.update { it.copy(isAdvancedMode = false) }
        }
    }
    
    /**
     * 구간 추가
     * 마지막 행의 null이었던 endTime을 확정하고, 새로운 무제한 행을 추가
     */
    private fun addFeeRow() {
        val currentRows = _state.value.advancedFeeRows.toMutableList()
        if (currentRows.isEmpty()) return // 방어 코드

        val lastIndex = currentRows.lastIndex
        val lastRow = currentRows[lastIndex]

        // 기존 마지막 행의 endTime 계산 (기본값: 시작시간 + 60분)
        val fixedEndTime = lastRow.endTime ?: (lastRow.startTime + 60)
        
        // 1. 기존 마지막 행 업데이트 (endTime 확정)
        currentRows[lastIndex] = lastRow.copy(endTime = fixedEndTime)

        // 2. 새 행 추가 (시작시간 = 이전 행 종료시간, 종료시간 = null)
        currentRows.add(
            AddParkingLotContract.FeeRow(
                startTime = fixedEndTime,
                endTime = null,
                unitMinutes = 10, // 기본값
                unitFee = 500,    // 기본값
                isFixedFee = false
            )
        )

        _state.update { it.copy(advancedFeeRows = currentRows) }
    }

    /**
     * 구간 삭제
     * 삭제된 구간만큼 다음 구간의 시작 시간을 앞당김
     */
    private fun removeFeeRow(index: Int) {
        val currentRows = _state.value.advancedFeeRows.toMutableList()
        
        // 첫 번째 행과 마지막 행은 삭제 불가
        if (index <= 0 || index == currentRows.lastIndex) return

        // 행 삭제
        currentRows.removeAt(index)
        
        // 리스트 재정렬 (Chain update)
        recalculateStartTimes(currentRows, startIndex = index)
        
        _state.update { it.copy(advancedFeeRows = currentRows) }
    }

    /**
     * 구간 정보 업데이트
     * startTime 변경 시 이전 행의 endTime도 변경
     * endTime 변경 시 다음 행의 startTime도 연쇄적으로 변경
     */
    private fun updateFeeRow(
        index: Int, 
        newStartTime: Int?, 
        newEndTime: Int?, 
        newUnitMinutes: Int?, 
        newUnitFee: Int?,
        newIsFixedFee: Boolean?
    ) {
        val currentRows = _state.value.advancedFeeRows.toMutableList()
        if (index !in currentRows.indices) return
        
        val row = currentRows[index]

        // startTime 업데이트
        if (newStartTime != null && newStartTime != row.startTime) {
            // 첫 번째 행은 startTime이 0이어야 함
            if (index == 0 && newStartTime != 0) {
                return
            }
            
            // startTime이 이전 행의 endTime과 다르면 이전 행의 endTime 업데이트
            if (index > 0) {
                val prevRow = currentRows[index - 1]
                if (prevRow.endTime != newStartTime) {
                    currentRows[index - 1] = prevRow.copy(endTime = newStartTime)
                }
            }
            
            // 현재 행의 startTime 업데이트
            currentRows[index] = row.copy(startTime = newStartTime)
            
            // endTime이 startTime보다 작거나 같으면 자동 조정
            if (row.endTime != null && row.endTime!! <= newStartTime) {
                currentRows[index] = currentRows[index].copy(endTime = newStartTime + 60)
            }
        }

        // endTime 업데이트
        if (newEndTime != null && newEndTime != row.endTime) {
            val currentStartTime = currentRows[index].startTime
            
            // 유효성 검사: endTime은 startTime보다 커야 함
            if (newEndTime <= currentStartTime) {
                return
            }

            // 현재 행의 endTime 업데이트
            currentRows[index] = currentRows[index].copy(endTime = newEndTime)

            // 다음 행이 있다면 다음 행들의 startTime 재계산 필요
            if (index < currentRows.lastIndex) {
                recalculateStartTimes(currentRows, startIndex = index + 1)
            }
        }

        // unitMinutes 업데이트
        if (newUnitMinutes != null && newUnitMinutes > 0) {
            currentRows[index] = currentRows[index].copy(unitMinutes = newUnitMinutes)
        }

        // unitFee 업데이트
        if (newUnitFee != null && newUnitFee >= 0) {
            currentRows[index] = currentRows[index].copy(unitFee = newUnitFee)
        }

        // isFixedFee 업데이트
        if (newIsFixedFee != null) {
            currentRows[index] = currentRows[index].copy(isFixedFee = newIsFixedFee)
        }

        _state.update { it.copy(advancedFeeRows = currentRows) }
    }

    /**
     * 리스트의 시간 연결성을 보장하기 위한 재귀/반복 계산
     */
    private fun recalculateStartTimes(rows: MutableList<AddParkingLotContract.FeeRow>, startIndex: Int) {
        for (i in startIndex until rows.size) {
            val prevEndTime = rows[i - 1].endTime ?: break // 이전 행이 무제한이면 여기서 끊김
            
            val currentRow = rows[i]
            // 시작 시간 갱신
            rows[i] = currentRow.copy(startTime = prevEndTime)
            
            // 현재 행의 endTime이 startTime보다 작거나 같아지는 모순 발생 시 자동 조정
            if (rows[i].endTime != null && rows[i].endTime!! <= rows[i].startTime) {
                rows[i] = rows[i].copy(endTime = rows[i].startTime + 60) // 강제 조정
            }
        }
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
                    val customFeeRules = zone.feeStructure?.customFeeRules ?: emptyList()
                    val hasCustomRules = customFeeRules.isNotEmpty()
                    
                    // CustomFeeRule이 있으면 고급 모드로 전환하고 FeeRow로 변환
                    val advancedFeeRows = if (hasCustomRules) {
                        convertCustomFeeRulesToFeeRows(customFeeRules)
                    } else {
                        emptyList()
                    }
                    
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            isEditMode = true,
                            editingZoneId = zoneId,
                            parkingLotName = zone.name,
                            useDefaultName = false,
                            isPublic = zone.isPublic,
                            isAdvancedMode = hasCustomRules,
                            advancedFeeRows = advancedFeeRows,
                            basicFeeDuration = zone.feeStructure?.basicFee?.durationMinutes ?: 30,
                            basicFeeAmount = zone.feeStructure?.basicFee?.fee ?: 1000,
                            additionalFeeInterval = zone.feeStructure?.additionalFee?.intervalMinutes ?: 10,
                            additionalFeeAmount = zone.feeStructure?.additionalFee?.fee ?: 500,
                            dailyMaxFeeEnabled = zone.feeStructure?.dailyMaxFee != null,
                            dailyMaxFeeAmount = zone.feeStructure?.dailyMaxFee?.maxFee ?: 10000,
                            customFeeRules = customFeeRules
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
