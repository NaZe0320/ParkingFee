package com.naze.parkingfee.presentation.ui.screens.history

import com.naze.parkingfee.domain.model.ParkingHistory

/**
 * 주차 기록 화면의 MVI Contract
 */
object HistoryContract {

    /**
     * 사용자 액션을 나타내는 Intent
     */
    sealed class HistoryIntent {
        object LoadHistories : HistoryIntent()
        data class DeleteHistory(val historyId: String) : HistoryIntent()
        object DeleteAllHistories : HistoryIntent()
        object NavigateBack : HistoryIntent()
    }

    /**
     * UI 상태를 나타내는 State
     */
    data class HistoryState(
        val isLoading: Boolean = false,
        val histories: List<ParkingHistory> = emptyList(),
        val errorMessage: String? = null
    )

    /**
     * 일회성 이벤트를 나타내는 Effect
     */
    sealed class HistoryEffect {
        data class ShowToast(val message: String) : HistoryEffect()
        object NavigateBack : HistoryEffect()
        data class ShowDeleteConfirmDialog(val historyId: String) : HistoryEffect()
        object ShowDeleteAllConfirmDialog : HistoryEffect()
    }
}
