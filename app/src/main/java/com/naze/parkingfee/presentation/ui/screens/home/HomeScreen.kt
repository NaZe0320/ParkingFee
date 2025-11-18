package com.naze.parkingfee.presentation.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naze.parkingfee.infrastructure.notification.ToastManager
import com.naze.parkingfee.presentation.ui.screens.home.components.*
import com.naze.parkingfee.presentation.ui.components.DeleteConfirmDialog
import com.naze.parkingfee.presentation.ui.components.ParkingCompleteDialog

/**
 * 홈 화면
 * MVI 패턴에 따라 State를 구독하고 UI를 렌더링합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToAddParkingLot: () -> Unit = {},
    onNavigateToZoneDetail: (String) -> Unit = {},
    onNavigateToEditZone: (String) -> Unit = {},
    onNavigateToEditVehicle: (String) -> Unit = {},
    onStartParkingService: () -> Unit = {},
    onStopParkingService: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 삭제 확인 다이얼로그 상태
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDeleteZoneId by remember { mutableStateOf<String?>(null) }
    var pendingDeleteZoneName by remember { mutableStateOf<String?>(null) }
    
    // 주차 완료 다이얼로그 상태
    var showParkingCompleteDialog by remember { mutableStateOf(false) }
    var parkingCompleteInfo by remember { mutableStateOf<HomeContract.HomeEffect.ShowParkingCompleteDialog?>(null) }

    // 화면 진입 시마다 주차장 목록 새로고침
    LaunchedEffect(Unit) {
        viewModel.processIntent(HomeContract.HomeIntent.RefreshParkingInfo)
    }

    // Effect 처리 - SharedFlow를 직접 collect
    LaunchedEffect(Unit) {
        viewModel.effect.collect { currentEffect ->
            when (currentEffect) {
                is HomeContract.HomeEffect.ShowToast -> {
                    ToastManager.show(context, currentEffect.message)
                }
                is HomeContract.HomeEffect.NavigateTo -> {
                    when (currentEffect.route) {
                        "settings" -> onNavigateToSettings()
                        "history" -> onNavigateToHistory()
                        "add_parking_lot" -> onNavigateToAddParkingLot()
                    }
                }
                is HomeContract.HomeEffect.NavigateToZoneDetail -> {
                    onNavigateToZoneDetail(currentEffect.zoneId)
                }
                is HomeContract.HomeEffect.NavigateToEditZone -> {
                    onNavigateToEditZone(currentEffect.zoneId)
                }
                is HomeContract.HomeEffect.ShowDeleteConfirmDialog -> {
                    pendingDeleteZoneId = currentEffect.zoneId
                    pendingDeleteZoneName = currentEffect.zoneName
                    showDeleteDialog = true
                }
                is HomeContract.HomeEffect.RequestStartParkingService -> {
                    onStartParkingService()
                }
                is HomeContract.HomeEffect.RequestStopParkingService -> {
                    onStopParkingService()
                }
                is HomeContract.HomeEffect.ShowParkingCompleteDialog -> {
                    parkingCompleteInfo = currentEffect
                    showParkingCompleteDialog = true
                }
                is HomeContract.HomeEffect.ShowAlarmScheduledToast -> {
                    ToastManager.show(context, "알람이 설정되었습니다.")
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
        // 고정 영역 - 주차 상태 카드
        ParkingStatusCard(
            isActive = state.isParkingActive,
            session = state.activeParkingSession,
            duration = state.parkingDuration,
            feeResult = com.naze.parkingfee.utils.FeeResult(state.parkingFee, state.parkingFee),
            vehicleDisplay = state.selectedVehicle?.let { v ->
                if (v.hasPlateNumber) "${v.displayName}(${v.displayPlateNumber})" else v.displayName
            },
            zoneName = state.activeZoneName,
            isExpanded = state.isStatusCardExpanded,
            onToggleExpand = {
                viewModel.processIntent(HomeContract.HomeIntent.ToggleStatusCard)
            },
            selectedZone = state.currentZone,
            onStartParking = { zoneId ->
                viewModel.processIntent(HomeContract.HomeIntent.StartParking(zoneId))
            },
            onStopParking = { sessionId ->
                viewModel.processIntent(HomeContract.HomeIntent.StopParking(sessionId))
            },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
        )

        // 스크롤 가능 영역
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                top = 0.dp,
                start = 0.dp,
                end = 0.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 주차 중일 때는 차량/주차장 선택 대신 무료 시간과 알람 설정 표시
            if (state.isParkingActive) {
                // 무료 시간 선택 섹션
                item {
                    FreeTimeSelector(
                        freeTimeMinutes = state.freeTimeMinutes,
                        onAddFreeTime = { minutes ->
                            viewModel.processIntent(HomeContract.HomeIntent.AddFreeTime(minutes))
                        },
                        onRemoveFreeTime = { minutes ->
                            viewModel.processIntent(HomeContract.HomeIntent.RemoveFreeTime(minutes))
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                
                // 알람 설정 섹션
                item {
                    AlarmSettingSection(
                        parkingAlarms = state.parkingAlarms,
                        onAddAlarm = { targetAmount, minutesBefore ->
                            viewModel.processIntent(
                                HomeContract.HomeIntent.AddAlarm(targetAmount, minutesBefore)
                            )
                        },
                        onRemoveAlarm = { alarmId ->
                            viewModel.processIntent(HomeContract.HomeIntent.RemoveAlarm(alarmId))
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                // 차량 선택 섹션 (주차 중이 아닐 때만)
                item {
                    VehicleSelector(
                        vehicles = state.vehicles,
                        selectedVehicle = state.selectedVehicle,
                        onVehicleSelected = { vehicle ->
                            viewModel.processIntent(HomeContract.HomeIntent.SelectVehicle(vehicle))
                        },
                        isExpanded = state.isVehicleSelectorExpanded,
                        onToggleExpand = {
                            viewModel.processIntent(HomeContract.HomeIntent.ToggleVehicleSelector)
                        }
                    )
                }

                // 주차장 선택 섹션 (주차 중이 아닐 때만)
                item {
                    ParkingZoneSelector(
                        zones = state.availableZones,
                        selectedZone = state.currentZone,
                        onZoneSelected = { zone ->
                            viewModel.processIntent(HomeContract.HomeIntent.SelectZone(zone))
                        },
                        isExpanded = state.isParkingZoneSelectorExpanded,
                        onToggleExpand = {
                            viewModel.processIntent(HomeContract.HomeIntent.ToggleParkingZoneSelector)
                        }
                    )
                }
            }

            // 에러 메시지
            if (state.errorMessage != null) {
                item {
                    ErrorMessage(
                        message = state.errorMessage,
                        onDismiss = {
                            viewModel.processIntent(HomeContract.HomeIntent.RefreshParkingInfo)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
        }
    }

    // 삭제 확인 다이얼로그
    if (pendingDeleteZoneName != null) {
        DeleteConfirmDialog(
            visible = showDeleteDialog,
            title = "주차장 삭제",
            itemName = pendingDeleteZoneName!!,
            message = "이 주차장을 삭제하시겠습니까?",
            onConfirm = {
                val id = pendingDeleteZoneId
                showDeleteDialog = false
                pendingDeleteZoneId = null
                pendingDeleteZoneName = null
                if (id != null) {
                    viewModel.processIntent(HomeContract.HomeIntent.DeleteZone(id))
                }
            },
            onDismiss = {
                showDeleteDialog = false
                pendingDeleteZoneId = null
                pendingDeleteZoneName = null
            }
        )
    }
    
    // 주차 완료 다이얼로그
    if (parkingCompleteInfo != null) {
        ParkingCompleteDialog(
            visible = showParkingCompleteDialog,
            zoneName = parkingCompleteInfo!!.zoneName,
            duration = parkingCompleteInfo!!.duration,
            vehicleDisplay = parkingCompleteInfo!!.vehicleDisplay,
            finalFee = parkingCompleteInfo!!.finalFee,
            originalFee = parkingCompleteInfo!!.originalFee,
            hasDiscount = parkingCompleteInfo!!.hasDiscount,
            onDismiss = {
                showParkingCompleteDialog = false
                parkingCompleteInfo = null
            }
        )
    }
}
