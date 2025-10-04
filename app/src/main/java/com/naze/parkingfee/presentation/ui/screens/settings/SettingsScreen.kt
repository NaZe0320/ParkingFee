package com.naze.parkingfee.presentation.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 설정 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToVehicleManagement: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(effect) {
        effect?.let { currentEffect ->
            when (currentEffect) {
                is SettingsContract.SettingsEffect.ShowToast -> {
                    // Toast 표시
                }
                is SettingsContract.SettingsEffect.NavigateBack -> {
                    onNavigateBack()
                }
                is SettingsContract.SettingsEffect.NavigateToVehicleManagement -> {
                    onNavigateToVehicleManagement()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.processIntent(SettingsContract.SettingsIntent.NavigateBack) }) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "알림 설정",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Switch(
                        checked = state.notificationEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.processIntent(SettingsContract.SettingsIntent.UpdateNotificationSetting(enabled))
                        }
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "자동 종료 설정",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Switch(
                        checked = state.autoStopEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.processIntent(SettingsContract.SettingsIntent.UpdateAutoStopSetting(enabled))
                        }
                    )
                }
            }
            
            // 차량 관리 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                onClick = { viewModel.processIntent(SettingsContract.SettingsIntent.NavigateToVehicleManagement) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "차량 관리",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "등록된 차량을 관리하고 할인 정보를 설정합니다",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("→")
                }
            }
        }
    }
}
