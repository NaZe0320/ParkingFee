package com.naze.parkingfee.presentation.ui.screens.parkinglots.add

import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.domain.model.FeeStructure
import com.naze.parkingfee.domain.model.BasicFeeRule
import com.naze.parkingfee.domain.model.AdditionalFeeRule
import com.naze.parkingfee.domain.model.DailyMaxFeeRule
import com.naze.parkingfee.domain.model.CustomFeeRule
import java.util.UUID

/**
 * 주차장 추가 화면의 MVI Contract
 * Intent, State, Effect를 정의합니다.
 */
object AddParkingLotContract {

    /**
     * UI 상태 관리를 위한 데이터 클래스 (고급 설정용)
     */
    data class FeeRow(
        val id: String = UUID.randomUUID().toString(),
        val startTime: Int,         // 시작 시간 (분) - 자동 계산
        val endTime: Int?,          // 종료 시간 (분) - null이면 '무제한/계속'
        val unitMinutes: Int,       // 과금 단위 시간 (분)
        val unitFee: Int,           // 단위 요금 (원)
        val isFixedFee: Boolean = false // 고정 요금 여부 (true면 구간 전체에 고정 요금 적용)
    )

    /**
     * 사용자 액션을 나타내는 Intent
     */
    sealed class AddParkingLotIntent {
        // 초기화
        object Initialize : AddParkingLotIntent()
        data class LoadZoneForEdit(val zoneId: String) : AddParkingLotIntent()
        
        // OCR 관련
        object OpenOcrScreen : AddParkingLotIntent()
        object ApplyOcrResult : AddParkingLotIntent()
        
        // 주차장 정보 입력
        data class UpdateParkingLotName(val name: String) : AddParkingLotIntent()
        data class ToggleUseDefaultName(val useDefault: Boolean) : AddParkingLotIntent()
        data class ToggleIsPublic(val isPublic: Boolean) : AddParkingLotIntent()
        
        // 기본 요금 체계
        data class UpdateBasicFeeDuration(val minutes: Int) : AddParkingLotIntent()
        data class UpdateBasicFeeAmount(val amount: Int) : AddParkingLotIntent()
        
        // 추가 요금 체계
        data class UpdateAdditionalFeeInterval(val minutes: Int) : AddParkingLotIntent()
        data class UpdateAdditionalFeeAmount(val amount: Int) : AddParkingLotIntent()
        
        // 일 최대 요금
        data class ToggleDailyMaxFee(val enabled: Boolean) : AddParkingLotIntent()
        data class UpdateDailyMaxFeeAmount(val amount: Int) : AddParkingLotIntent()
        
        // 커스텀 요금 구간
        object AddCustomFeeRule : AddParkingLotIntent()
        data class RemoveCustomFeeRule(val index: Int) : AddParkingLotIntent()
        data class UpdateCustomFeeRule(
            val index: Int,
            val minMinutes: Int,
            val maxMinutes: Int?,
            val fee: Int
        ) : AddParkingLotIntent()
        
        // 주차장 저장
        object SaveParkingLot : AddParkingLotIntent()
        
        // 화면 닫기
        object NavigateBack : AddParkingLotIntent()
        
        // 폼 초기화
        object ResetForm : AddParkingLotIntent()
        
        // 고급 모드 관련
        data class ToggleAdvancedMode(val enabled: Boolean) : AddParkingLotIntent()
        object AddFeeRow : AddParkingLotIntent()
        data class RemoveFeeRow(val index: Int) : AddParkingLotIntent()
        data class UpdateFeeRow(
            val index: Int,
            val startTime: Int? = null,
            val endTime: Int? = null,
            val unitMinutes: Int? = null,
            val unitFee: Int? = null,
            val isFixedFee: Boolean? = null
        ) : AddParkingLotIntent()
    }

    /**
     * UI 상태를 나타내는 State
     */
    data class AddParkingLotState(
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        
        // 편집 모드 관련
        val isEditMode: Boolean = false,
        val editingZoneId: String? = null,
        
        // 주차장 기본 정보
        val parkingLotName: String = "",
        val useDefaultName: Boolean = true,
        val isPublic: Boolean = false, // 공영 주차장 여부
        
        // 모드 설정
        val isAdvancedMode: Boolean = false, // false: 단순 모드, true: 고급 모드
        val advancedFeeRows: List<FeeRow> = emptyList(), // 고급 모드용 데이터
        
        // 기본 요금 체계 (단순 모드용)
        val basicFeeDuration: Int = 30, // 기본 30분
        val basicFeeAmount: Int = 1000, // 기본 1000원
        
        // 추가 요금 체계
        val additionalFeeInterval: Int = 10, // 기본 10분
        val additionalFeeAmount: Int = 500, // 기본 500원
        
        // 일 최대 요금
        val dailyMaxFeeEnabled: Boolean = false,
        val dailyMaxFeeAmount: Int = 10000, // 기본 10000원
        
        // 커스텀 요금 구간들
        val customFeeRules: List<CustomFeeRule> = emptyList(),
        
        // 에러 메시지
        val errorMessage: String? = null,
        val validationErrors: Map<String, String> = emptyMap(),
        
        // 성공 상태
        val isSaved: Boolean = false
    )

    /**
     * 일회성 이벤트를 나타내는 Effect
     */
    sealed class AddParkingLotEffect {
        data class ShowToast(val message: String) : AddParkingLotEffect()
        data class NavigateTo(val route: String) : AddParkingLotEffect()
        data class ShowDialog(val title: String, val message: String) : AddParkingLotEffect()
        data class ShowValidationError(val field: String, val message: String) : AddParkingLotEffect()
        object NavigateBack : AddParkingLotEffect()
        object OpenOcrScreen : AddParkingLotEffect()
    }
}
