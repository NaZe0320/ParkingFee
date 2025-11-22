package com.naze.parkingfee.presentation.ui.screens.ocr

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.parkingfee.data.datasource.local.ocr.OcrProcessor
import com.naze.parkingfee.data.datasource.local.ocr.ParkingFeeParser
import com.naze.parkingfee.presentation.ui.screens.ocr.OcrResultManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * OCR 화면의 ViewModel
 * MVI 패턴에 따라 Intent를 처리하고 State를 업데이트합니다.
 */
@HiltViewModel
class OcrViewModel @Inject constructor(
    private val ocrProcessor: OcrProcessor
) : ViewModel() {

    // State
    private val _state = MutableStateFlow(OcrContract.OcrState())
    val state: StateFlow<OcrContract.OcrState> = _state.asStateFlow()

    // Effect
    private val _effect = MutableSharedFlow<OcrContract.OcrEffect>()
    val effect = _effect.asSharedFlow()

    /**
     * Intent 처리
     */
    fun processIntent(intent: OcrContract.OcrIntent) {
        when (intent) {
            is OcrContract.OcrIntent.Initialize -> initialize()
            is OcrContract.OcrIntent.SelectImageFromGallery -> selectImageFromGallery()
            is OcrContract.OcrIntent.CaptureImageFromCamera -> captureImageFromCamera()
            is OcrContract.OcrIntent.ImageSelected -> onImageSelected(intent.uri)
            is OcrContract.OcrIntent.ProcessOcr -> processOcr()
            is OcrContract.OcrIntent.UseOcrResult -> useOcrResult()
            is OcrContract.OcrIntent.Reset -> reset()
            is OcrContract.OcrIntent.NavigateBack -> navigateBack()
        }
    }

    /**
     * 초기화
     */
    private fun initialize() {
        _state.update {
            OcrContract.OcrState()
        }
    }

    /**
     * 갤러리에서 이미지 선택
     */
    private fun selectImageFromGallery() {
        viewModelScope.launch {
            _effect.emit(OcrContract.OcrEffect.OpenGallery)
        }
    }

    /**
     * 카메라로 이미지 촬영
     */
    private fun captureImageFromCamera() {
        viewModelScope.launch {
            _effect.emit(OcrContract.OcrEffect.OpenCamera)
        }
    }

    /**
     * 이미지 선택 완료
     */
    private fun onImageSelected(uri: Uri) {
        _state.update {
            it.copy(
                selectedImageUri = uri,
                hasImage = true,
                recognizedText = null,
                hasResult = false,
                errorMessage = null
            )
        }
    }

    /**
     * OCR 처리 시작
     */
    private fun processOcr() {
        val imageUri = _state.value.selectedImageUri
        
        if (imageUri == null) {
            viewModelScope.launch {
                _effect.emit(OcrContract.OcrEffect.ShowToast("먼저 이미지를 선택해주세요."))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, errorMessage = null) }

            try {
                // OCR 처리
                val result = ocrProcessor.recognizeTextFromUri(imageUri)

                if (result.isSuccess) {
                    // 주차장 정보 파싱 (기존 로직)
                    val parsedInfo = ocrProcessor.parseParkingLotInfo(result)

                    // 요금표 파싱 (ParkingFeeParser 사용)
                    val parsingResult = ParkingFeeParser.parse(
                        text = result.fullText,
                        textBlocks = result.textBlocks
                    )

                    if (parsingResult.isSuccess && parsingResult.feeRows.isNotEmpty()) {
                        // 파싱 성공: 편집 화면 표시
                        _state.update {
                            it.copy(
                                isProcessing = false,
                                recognizedText = result.fullText,
                                hasResult = true,
                                parsedParkingLotName = parsedInfo.parkingLotName,
                                parsedFeeInfo = parsedInfo.feeInfo,
                                parsedIsPublic = parsedInfo.isPublic,
                                feeRows = parsingResult.feeRows,
                                dailyMaxFee = parsingResult.dailyMaxFee,
                                showEditScreen = true,
                                errorMessage = null
                            )
                        }
                        _effect.emit(
                            OcrContract.OcrEffect.ShowToast(
                                "인식된 결과입니다. 정확한지 확인해주세요."
                            )
                        )
                    } else {
                        // 파싱 실패: 인식된 텍스트만 표시
                        _state.update {
                            it.copy(
                                isProcessing = false,
                                recognizedText = result.fullText,
                                hasResult = true,
                                parsedParkingLotName = parsedInfo.parkingLotName,
                                parsedFeeInfo = parsedInfo.feeInfo,
                                parsedIsPublic = parsedInfo.isPublic,
                                feeRows = emptyList(),
                                dailyMaxFee = null,
                                showEditScreen = false,
                                errorMessage = null
                            )
                        }
                        
                        if (result.fullText.isBlank()) {
                            _effect.emit(
                                OcrContract.OcrEffect.ShowToast("요금표를 인식하지 못했습니다.")
                            )
                        } else {
                            _effect.emit(
                                OcrContract.OcrEffect.ShowToast("요금표를 인식하지 못했습니다.")
                            )
                        }
                    }
                } else {
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = result.errorMessage
                        )
                    }
                    _effect.emit(
                        OcrContract.OcrEffect.ShowToast(
                            result.errorMessage ?: "텍스트 인식에 실패했습니다."
                        )
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = e.message
                    )
                }
                _effect.emit(
                    OcrContract.OcrEffect.ShowToast("오류가 발생했습니다: ${e.message}")
                )
            }
        }
    }

    /**
     * OCR 결과 사용 (AddParkingLotScreen으로 전달)
     */
    private fun useOcrResult() {
        viewModelScope.launch {
            val state = _state.value
            
            // OcrResultManager에 결과 저장
            OcrResultManager.saveResult(
                OcrResultManager.OcrResult(
                    parkingLotName = state.parsedParkingLotName,
                    feeRows = state.feeRows,
                    dailyMaxFee = state.dailyMaxFee,
                    isPublic = state.parsedIsPublic
                )
            )

            _effect.emit(
                OcrContract.OcrEffect.NavigateToAddParkingLotWithResult(
                    parkingLotName = state.parsedParkingLotName,
                    feeInfo = state.parsedFeeInfo,
                    feeRows = state.feeRows,
                    dailyMaxFee = state.dailyMaxFee
                )
            )
        }
    }

    /**
     * 초기화 (새로운 이미지 선택을 위해)
     */
    private fun reset() {
        _state.update {
            OcrContract.OcrState(
                showEditScreen = false
            )
        }
    }

    /**
     * 뒤로 가기
     */
    private fun navigateBack() {
        viewModelScope.launch {
            _effect.emit(OcrContract.OcrEffect.NavigateBack)
        }
    }

    /**
     * 리소스 정리 (화면을 나갈 때 명시적으로 호출)
     */
    fun cleanup() {
        ocrProcessor.close()
    }

    /**
     * ViewModel이 정리될 때 리소스 해제
     */
    override fun onCleared() {
        super.onCleared()
        ocrProcessor.close()
    }
}

