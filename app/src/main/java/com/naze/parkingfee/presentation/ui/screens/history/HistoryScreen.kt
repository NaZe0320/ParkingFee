package com.naze.parkingfee.presentation.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naze.parkingfee.domain.model.ParkingHistory
import com.naze.parkingfee.infrastructure.notification.ToastManager

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
    val context = LocalContext.current
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var deleteHistoryId by remember { mutableStateOf<String?>(null) }

    // Effect 처리
    LaunchedEffect(effect) {
        effect?.let { currentEffect ->
            when (currentEffect) {
                is HistoryContract.HistoryEffect.ShowToast -> {
                    ToastManager.show(context, currentEffect.message)
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
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            ),
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5) // 연한 회색 배경
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                // 삭제 버튼 (빨간색 아이콘)
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "삭제",
                        tint = Color(0xFFE57373), // 연한 빨간색
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // 날짜/시간 정보
            Text(
                text = history.getFormattedDate(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666) // 회색 텍스트
            )
            
            // 차량 정보 (있는 경우)
            if (!history.vehicleNameSnapshot.isNullOrBlank()) {
                val vehicleDisplay = if (!history.vehiclePlateSnapshot.isNullOrBlank()) {
                    "${history.vehicleNameSnapshot}(${history.vehiclePlateSnapshot})"
                } else {
                    history.vehicleNameSnapshot
                }
                Text(
                    text = vehicleDisplay,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666)
                )
            }
            
            // 구분선
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFE0E0E0))
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            // 하단: 주차 시간과 요금 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 좌측: 주차 시간과 요금
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 주차 시간
                    Column {
                        Text(
                            text = "주차 시간",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = history.getFormattedDuration(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    
                    // 세로 구분선
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color(0xFFE0E0E0))
                    )
                    
                    // 요금
                    Column {
                        Text(
                            text = "요금",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF666666)
                        )
                        if (history.hasDiscount && history.originalFee != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${history.originalFee.toInt()}원",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF999999),
                                    textDecoration = TextDecoration.LineThrough
                                )
                                Text(
                                    text = "→ ${history.feePaid.toInt()}원",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        } else {
                            Text(
                                text = "${history.feePaid.toInt()}원",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
                
                // 우측: 할인 배지 (있는 경우)
                if (history.hasDiscount) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF4CAF50)) // 녹색 배경
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "50% 할인",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
