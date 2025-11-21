package com.naze.parkingfee.presentation.ui.screens.ocr

import android.net.Uri
import com.naze.parkingfee.presentation.ui.screens.parkinglots.add.AddParkingLotContract

/**
 * OCR 화면의 MVI Contract
 * Intent, State, Effect를 정의합니다.
 */
object OcrContract {

    /**
     * 사용자 액션을 나타내는 Intent
     */
    sealed class OcrIntent {
        // 초기화
        object Initialize : OcrIntent()
        
        // 이미지 선택/촬영
        object SelectImageFromGallery : OcrIntent()
        object CaptureImageFromCamera : OcrIntent()
        data class ImageSelected(val uri: Uri) : OcrIntent()
        
        // OCR 실행
        object ProcessOcr : OcrIntent()
        
        // 결과 사용
        object UseOcrResult : OcrIntent()
        
        // 초기화
        object Reset : OcrIntent()
        
        // 뒤로 가기
        object NavigateBack : OcrIntent()
    }

    /**
     * UI 상태를 나타내는 State
     */
    data class OcrState(
        val isLoading: Boolean = false,
        val isProcessing: Boolean = false,
        
        // 이미지 관련
        val selectedImageUri: Uri? = null,
        val hasImage: Boolean = false,
        
        // OCR 결과
        val recognizedText: String? = null,
        val hasResult: Boolean = false,
        
        // 파싱된 데이터 (나중에 AddParkingLotScreen에 전달)
        val parsedParkingLotName: String? = null,
        val parsedFeeInfo: String? = null,
        
        // 파싱된 요금 정보
        val feeRows: List<AddParkingLotContract.FeeRow> = emptyList(),
        val dailyMaxFee: Int? = null,
        val showEditScreen: Boolean = false, // 편집 화면 표시 여부
        
        // 에러 메시지
        val errorMessage: String? = null
    )

    /**
     * 일회성 이벤트를 나타내는 Effect
     */
    sealed class OcrEffect {
        data class ShowToast(val message: String) : OcrEffect()
        object NavigateBack : OcrEffect()
        object OpenGallery : OcrEffect()
        object OpenCamera : OcrEffect()
        data class NavigateToAddParkingLotWithResult(
            val parkingLotName: String?,
            val feeInfo: String?,
            val feeRows: List<AddParkingLotContract.FeeRow>,
            val dailyMaxFee: Int?
        ) : OcrEffect()
    }
}

