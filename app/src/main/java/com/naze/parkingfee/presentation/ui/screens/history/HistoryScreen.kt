package com.naze.parkingfee.presentation.ui.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 헤더와 삭제 버튼
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "주차 기록",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // if (state.histories.isNotEmpty()) {
                //     OutlinedButton(
                //         onClick = { viewModel.processIntent(HistoryContract.HistoryIntent.DeleteAllHistories) },
                //         colors = ButtonDefaults.outlinedButtonColors(
                //             contentColor = MaterialTheme.colorScheme.error
                //         ),
                //         border = BorderStroke(
                //             1.dp,
                //             MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                //         ),
                //         shape = RoundedCornerShape(12.dp)
                //     ) {
                //         Icon(Icons.Default.Delete, contentDescription = "모든 기록 삭제", modifier = Modifier.size(16.dp))
                //         Spacer(modifier = Modifier.width(4.dp))
                //         Text("전체 삭제", style = MaterialTheme.typography.bodySmall)
                //     }
                // }
            }
        }

        // 상태 표시
        when {
            state.isLoading -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            state.histories.isEmpty() -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "주차 기록이 없습니다",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "주차를 시작하면 기록이 저장됩니다",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            else -> {
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 상단: 주차장 이름과 삭제 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = history.zoneNameSnapshot,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            // 날짜와 시간 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "시작",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = history.getFormattedStartDate(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Column {
                    Text(
                        text = "주차 시간",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = history.getFormattedDuration(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // 차량 정보
            if (!history.vehicleNameSnapshot.isNullOrBlank()) {
                val vehicleDisplay = if (!history.vehiclePlateSnapshot.isNullOrBlank()) {
                    "${history.vehicleNameSnapshot}(${history.vehiclePlateSnapshot})"
                } else {
                    history.vehicleNameSnapshot
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "차량",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = vehicleDisplay,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // 요금 정보 (할인 정보 포함)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (history.hasDiscount && history.originalFee != null) {
                    Column {
                        Text(
                            text = "요금",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${history.originalFee.toInt()}원",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = TextDecoration.LineThrough
                            )
                            Text(
                                text = "→ ${history.feePaid.toInt()}원",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // 할인 배지
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "50% 할인",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Column {
                        Text(
                            text = "요금",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${history.feePaid.toInt()}원",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
