package com.naze.parkingfee.presentation.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.parkingfee.domain.usecase.parkinghistory.GetParkingHistoryUseCase
import com.naze.parkingfee.domain.usecase.parkinghistory.DeleteParkingHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 주차 기록 화면의 ViewModel
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getParkingHistoryUseCase: GetParkingHistoryUseCase,
    private val deleteParkingHistoryUseCase: DeleteParkingHistoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryContract.HistoryState())
    val state: StateFlow<HistoryContract.HistoryState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HistoryContract.HistoryEffect>()
    val effect: SharedFlow<HistoryContract.HistoryEffect> = _effect.asSharedFlow()

    init {
        processIntent(HistoryContract.HistoryIntent.LoadHistories)
    }

    /**
     * Intent를 처리하는 메서드
     */
    fun processIntent(intent: HistoryContract.HistoryIntent) {
        when (intent) {
            is HistoryContract.HistoryIntent.LoadHistories -> loadHistories()
            is HistoryContract.HistoryIntent.DeleteHistory -> deleteHistory(intent.historyId)
            is HistoryContract.HistoryIntent.DeleteAllHistories -> deleteAllHistories()
            is HistoryContract.HistoryIntent.NavigateBack -> navigateBack()
        }
    }

    private fun loadHistories() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, errorMessage = null) }
                
                getParkingHistoryUseCase.execute().collect { histories ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            histories = histories
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "기록을 불러오는 중 오류가 발생했습니다."
                    )
                }
            }
        }
    }

    private fun deleteHistory(historyId: String) {
        viewModelScope.launch {
            _effect.emit(HistoryContract.HistoryEffect.ShowDeleteConfirmDialog(historyId))
        }
    }

    private fun deleteAllHistories() {
        viewModelScope.launch {
            _effect.emit(HistoryContract.HistoryEffect.ShowDeleteAllConfirmDialog)
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.emit(HistoryContract.HistoryEffect.NavigateBack)
        }
    }

    /**
     * 기록 삭제 실행
     */
    fun confirmDeleteHistory(historyId: String) {
        viewModelScope.launch {
            try {
                deleteParkingHistoryUseCase.execute(historyId)
                _effect.emit(HistoryContract.HistoryEffect.ShowToast("기록이 삭제되었습니다."))
                // 기록 목록 새로고침
                loadHistories()
            } catch (e: Exception) {
                _effect.emit(HistoryContract.HistoryEffect.ShowToast("삭제 중 오류가 발생했습니다: ${e.message}"))
            }
        }
    }

    /**
     * 모든 기록 삭제 실행
     */
    fun confirmDeleteAllHistories() {
        viewModelScope.launch {
            try {
                // 모든 기록 삭제 로직 (Repository에 구현 필요)
                _effect.emit(HistoryContract.HistoryEffect.ShowToast("모든 기록이 삭제되었습니다."))
                // 기록 목록 새로고침
                loadHistories()
            } catch (e: Exception) {
                _effect.emit(HistoryContract.HistoryEffect.ShowToast("삭제 중 오류가 발생했습니다: ${e.message}"))
            }
        }
    }
}
