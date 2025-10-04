package com.naze.parkingfee.presentation.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naze.parkingfee.domain.model.ParkingHistory

/**
 * 주차 기록 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle(initialValue = null)
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var deleteHistoryId by remember { mutableStateOf<String?>(null) }

    // Effect 처리
    LaunchedEffect(effect) {
        effect?.let { currentEffect ->
            when (currentEffect) {
                is HistoryContract.HistoryEffect.ShowToast -> {
                    // Toast 표시 로직
                }
                is HistoryContract.HistoryEffect.NavigateBack -> {
                    onNavigateBack()
                }
                is HistoryContract.HistoryEffect.ShowDeleteConfirmDialog -> {
                    deleteHistoryId = currentEffect.historyId
                    showDeleteDialog = true
                }
                is HistoryContract.HistoryEffect.ShowDeleteAllConfirmDialog -> {
                    showDeleteAllDialog = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("주차 기록") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.processIntent(HistoryContract.HistoryIntent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    if (state.histories.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.processIntent(HistoryContract.HistoryIntent.DeleteAllHistories) }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "모든 기록 삭제")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.histories.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "주차 기록이 없습니다",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "주차를 시작하면 기록이 저장됩니다",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.histories) { history ->
                        HistoryItem(
                            history = history,
                            onDelete = { 
                                viewModel.processIntent(HistoryContract.HistoryIntent.DeleteHistory(history.id))
                            }
                        )
                    }
                }
            }
        }
    }

    // 개별 기록 삭제 확인 다이얼로그
    if (showDeleteDialog && deleteHistoryId != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                deleteHistoryId = null
            },
            title = { Text("기록 삭제") },
            text = { Text("정말로 이 주차 기록을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        deleteHistoryId?.let { id ->
                            viewModel.confirmDeleteHistory(id)
                        }
                        deleteHistoryId = null
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteDialog = false
                        deleteHistoryId = null
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }

    // 모든 기록 삭제 확인 다이얼로그
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("모든 기록 삭제") },
            text = { Text("정말로 모든 주차 기록을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAllDialog = false
                        viewModel.confirmDeleteAllHistories()
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
private fun HistoryItem(
    history: ParkingHistory,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = history.zoneNameSnapshot,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = history.getFormattedStartDate(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "주차 시간: ${history.getFormattedDuration()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // 차량 정보 표시
                if (!history.vehicleNameSnapshot.isNullOrBlank()) {
                    val vehicleDisplay = if (!history.vehiclePlateSnapshot.isNullOrBlank()) {
                        "${history.vehicleNameSnapshot}(${history.vehiclePlateSnapshot})"
                    } else {
                        history.vehicleNameSnapshot
                    }
                    Text(
                        text = "차량: $vehicleDisplay",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 요금 표시 (할인 정보 포함)
                if (history.hasDiscount && history.originalFee != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "요금: ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${history.originalFee.toInt()}원",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Text(
                            text = "→ ${history.feePaid.toInt()}원",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "(50% 할인)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Text(
                        text = "요금: ${history.feePaid.toInt()}원",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
