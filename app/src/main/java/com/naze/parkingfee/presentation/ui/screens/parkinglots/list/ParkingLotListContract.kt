package com.naze.parkingfee.presentation.ui.screens.settings.parkinglots.list

import com.naze.parkingfee.domain.model.ParkingZone

/**
 * 주차장 목록 화면의 MVI Contract
 * Intent, State, Effect를 정의합니다.
 */
object ParkingLotListContract {

    /**
     * 사용자 액션을 나타내는 Intent
     */
    sealed class ParkingLotListIntent {
        // 초기화
        object LoadParkingLots : ParkingLotListIntent()
        
        // 네비게이션
        object NavigateToAddParkingLot : ParkingLotListIntent()
        data class NavigateToEditParkingLot(val zoneId: String) : ParkingLotListIntent()
        
        // 주차장 관리
        data class DeleteParkingLot(val zoneId: String) : ParkingLotListIntent()
        data class ToggleFavorite(val zoneId: String) : ParkingLotListIntent()
        
        // 정렬
        data class ChangeSortOrder(val sortOrder: SortOrder) : ParkingLotListIntent()
    }

    /**
     * UI 상태를 나타내는 State
     */
    data class ParkingLotListState(
        val parkingLots: List<ParkingZone> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val sortOrder: SortOrder = SortOrder.NAME
    )

    /**
     * 일회성 이벤트를 나타내는 Effect
     */
    sealed class ParkingLotListEffect {
        data class ShowToast(val message: String) : ParkingLotListEffect()
        object NavigateToAddParkingLot : ParkingLotListEffect()
        data class NavigateToEditParkingLot(val zoneId: String) : ParkingLotListEffect()
        data class ShowDeleteConfirmation(val zoneId: String, val zoneName: String) : ParkingLotListEffect()
    }

    /**
     * 정렬 순서 열거형
     */
    enum class SortOrder {
        NAME,      // 이름순
        RECENT,    // 최근 사용순
        FAVORITE   // 즐겨찾기순
    }
}
