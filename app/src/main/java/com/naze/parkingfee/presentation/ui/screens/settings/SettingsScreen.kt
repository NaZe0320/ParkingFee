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
    onNavigateBack: () -> Unit = {}
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
        }
    }
}
