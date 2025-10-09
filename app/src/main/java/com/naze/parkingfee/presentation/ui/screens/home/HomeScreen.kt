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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naze.parkingfee.presentation.ui.screens.home.components.*
import com.naze.parkingfee.presentation.ui.components.DeleteConfirmDialog

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
    onStartParkingService: () -> Unit = {},
    onStopParkingService: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle(initialValue = null)

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

    // Effect 처리
    LaunchedEffect(effect) {
        effect?.let { currentEffect ->
            when (currentEffect) {
                is HomeContract.HomeEffect.ShowToast -> {
                    // Toast 표시 로직 (Snackbar 등)
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
                is HomeContract.HomeEffect.ShowDialog -> {
                    // Dialog 표시 로직
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
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
            isExpanded = state.isStatusCardExpanded,
            onToggleExpand = {
                viewModel.processIntent(HomeContract.HomeIntent.ToggleStatusCard)
            },
            controlButtons = {
                ParkingControlButtons(
                    isParkingActive = state.isParkingActive,
                    selectedZone = state.currentZone,
                    activeSession = state.activeParkingSession,
                    onStartParking = { zoneId ->
                        viewModel.processIntent(HomeContract.HomeIntent.StartParking(zoneId))
                    },
                    onStopParking = { sessionId ->
                        viewModel.processIntent(HomeContract.HomeIntent.StopParking(sessionId))
                    }
                )
            }
        )

        // 스크롤 가능 영역
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.weight(1f)
        ) {
            // 차량 선택 섹션
            item {
                VehicleSelector(
                    vehicles = emptyList(), // TODO: 실제 차량 목록 구현
                    selectedVehicle = state.selectedVehicle,
                    onVehicleSelected = { vehicle ->
                        viewModel.processIntent(HomeContract.HomeIntent.SelectVehicle(vehicle))
                    }
                )
            }

            // 주차장 선택 섹션
            item {
                ParkingZoneSelector(
                    zones = state.availableZones,
                    selectedZone = state.currentZone,
                    onZoneSelected = { zone ->
                        viewModel.processIntent(HomeContract.HomeIntent.SelectZone(zone))
                    },
                    onRequestZoneAction = { zone, action ->
                        viewModel.processIntent(HomeContract.HomeIntent.RequestZoneAction(zone, action))
                    }
                )
            }

            // 에러 메시지
            if (state.errorMessage != null) {
                item {
                    ErrorMessage(
                        message = state.errorMessage,
                        onDismiss = {
                            viewModel.processIntent(HomeContract.HomeIntent.RefreshParkingInfo)
                        }
                    )
                }
            }
        }
    }


    // 삭제 확인 다이얼로그
    DeleteConfirmDialog(
        visible = showDeleteDialog,
        message = "정말로 ${pendingDeleteZoneName ?: "이"} 구역을 삭제하시겠습니까?",
        onConfirm = {
            val id = pendingDeleteZoneId
            showDeleteDialog = false
            pendingDeleteZoneId = null
            pendingDeleteZoneName = null
            if (id != null) {
                viewModel.processIntent(HomeContract.HomeIntent.DeleteZone(id))
            }
        },
        onDismiss = { showDeleteDialog = false }
    )
    
    // 주차 완료 다이얼로그
    if (showParkingCompleteDialog && parkingCompleteInfo != null) {
        AlertDialog(
            onDismissRequest = { 
                showParkingCompleteDialog = false
                parkingCompleteInfo = null
            },
            title = { 
                Text(
                    text = "주차 완료",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text(
                        text = "구역: ${parkingCompleteInfo!!.zoneName}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "주차 시간: ${parkingCompleteInfo!!.duration}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (!parkingCompleteInfo!!.vehicleDisplay.isNullOrBlank()) {
                        Text(
                            text = "차량: ${parkingCompleteInfo!!.vehicleDisplay}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    if (parkingCompleteInfo!!.hasDiscount && parkingCompleteInfo!!.originalFee != null) {
                        Text(
                            text = "요금: ${parkingCompleteInfo!!.originalFee!!.toInt()}원 → ${parkingCompleteInfo!!.finalFee.toInt()}원",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "50% 할인 적용",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "요금: ${parkingCompleteInfo!!.finalFee.toInt()}원",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showParkingCompleteDialog = false
                        parkingCompleteInfo = null
                    }
                ) {
                    Text("확인")
                }
            }
        )
    }
}
