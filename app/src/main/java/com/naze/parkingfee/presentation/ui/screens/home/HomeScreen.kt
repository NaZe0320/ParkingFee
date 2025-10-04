package com.naze.parkingfee.presentation.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
                    // 삭제 확인 다이얼로그 표시 로직
                }
                is HomeContract.HomeEffect.RequestStartParkingService -> {
                    onStartParkingService()
                }
                is HomeContract.HomeEffect.RequestStopParkingService -> {
                    onStopParkingService()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("주차 요금 계산기") },
                actions = {
                    IconButton(onClick = { viewModel.processIntent(HomeContract.HomeIntent.NavigateToHistory) }) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "주차 기록"
                        )
                    }
                    IconButton(onClick = { viewModel.processIntent(HomeContract.HomeIntent.NavigateToSettings) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "설정"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.processIntent(HomeContract.HomeIntent.NavigateToAddParkingLot) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.semantics {
                    contentDescription = "주차장 추가"
                }
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

            ParkingStatusCard(
                isActive = state.isParkingActive,
                session = state.activeParkingSession,
                duration = state.parkingDuration,
                fee = state.parkingFee
            )

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

            if (state.errorMessage != null) {
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
