package com.naze.parkingfee.presentation.ui.screens.ocr

import com.naze.parkingfee.presentation.ui.screens.parkinglots.add.AddParkingLotContract

/**
 * OCR 결과를 임시로 저장하고 전달하는 싱글톤 Manager
 * OcrScreen에서 AddParkingLotScreen으로 복잡한 객체를 전달하기 위해 사용
 */
object OcrResultManager {
    
    /**
     * OCR 결과 데이터 클래스
     */
    data class OcrResult(
        val parkingLotName: String?,
        val feeRows: List<AddParkingLotContract.FeeRow>,
        val dailyMaxFee: Int?,
        val isPublic: Boolean = false
    )
    
    // 임시 저장소
    private var ocrResult: OcrResult? = null
    
    /**
     * OCR 결과 저장
     */
    fun saveResult(result: OcrResult) {
        ocrResult = result
    }
    
    /**
     * OCR 결과 가져오기
     */
    fun getResult(): OcrResult? {
        return ocrResult
    }
    
    /**
     * OCR 결과 삭제 (사용 완료 후 호출)
     */
    fun clearResult() {
        ocrResult = null
    }
}

