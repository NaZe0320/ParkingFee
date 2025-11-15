package com.naze.parkingfee.presentation.ui.screens.zonedetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naze.parkingfee.infrastructure.notification.ToastManager
import com.naze.parkingfee.presentation.ui.components.DeleteConfirmDialog

/**
 * 주차 구역 상세 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneDetailScreen(
    zoneId: String,
    viewModel: ZoneDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 구역 ID 설정
    LaunchedEffect(zoneId) {
        viewModel.setZoneId(zoneId)
    }

    // Effect 처리 - SharedFlow를 직접 collect하여 모든 Effect를 순차적으로 처리
    LaunchedEffect(Unit) {
        viewModel.effect.collect { currentEffect ->
            when (currentEffect) {
                is ZoneDetailContract.ZoneDetailEffect.ShowToast -> {
                    ToastManager.show(context, currentEffect.message)
                }
                is ZoneDetailContract.ZoneDetailEffect.NavigateToEdit -> {
                    onNavigateToEdit(currentEffect.zoneId)
                }
                is ZoneDetailContract.ZoneDetailEffect.NavigateBack -> {
                    onNavigateBack()
                }
                is ZoneDetailContract.ZoneDetailEffect.ShowDeleteConfirmDialog -> {
                    showDeleteDialog = true
                }
                is ZoneDetailContract.ZoneDetailEffect.NavigateToHome -> {
                    onNavigateToHome()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("구역 상세") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.processIntent(ZoneDetailContract.ZoneDetailIntent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.processIntent(ZoneDetailContract.ZoneDetailIntent.NavigateToEdit) }) {
                        Icon(Icons.Default.Edit, contentDescription = "편집")
                    }
                    IconButton(onClick = { viewModel.processIntent(ZoneDetailContract.ZoneDetailIntent.DeleteZone) }) {
                        Icon(Icons.Default.Delete, contentDescription = "삭제")
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
            state.zone != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 구역 기본 정보
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "구역 정보",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            InfoRow("구역명", state.zone!!.name)
                            InfoRow("요금 정보", state.zone!!.getDisplayFeeInfo())
                            InfoRow("최대 수용량", "${state.zone!!.maxCapacity}대")
                            InfoRow("현재 주차", "${state.zone!!.currentOccupancy}대")
                            InfoRow("가용 여부", if (state.zone!!.isAvailable) "가능" else "불가능")
                        }
                    }

                    // 요금 체계 상세 정보 (복잡한 요금 체계가 있는 경우)
                    state.zone!!.feeStructure?.let { feeStructure ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "요금 체계",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                InfoRow("기본 요금", "${feeStructure.basicFee.fee}원 (${feeStructure.basicFee.durationMinutes}분)")
                                InfoRow("추가 요금", "${feeStructure.additionalFee.fee}원 (${feeStructure.additionalFee.intervalMinutes}분마다)")
                                
                                feeStructure.dailyMaxFee?.let { dailyMax ->
                                    InfoRow("일 최대 요금", "${dailyMax.maxFee}원")
                                }
                                
                                if (feeStructure.customFeeRules.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "커스텀 요금 구간",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    feeStructure.customFeeRules.forEach { rule ->
                                        val maxText = rule.maxMinutes?.let { "~${it}분" } ?: "이상"
                                        InfoRow("", "${rule.minMinutes}분 ${maxText}: ${rule.fee}원")
                                    }
                                }
                            }
                        }
                    }

                    // 선택하기 버튼
                    Button(
                        onClick = { viewModel.processIntent(ZoneDetailContract.ZoneDetailIntent.SelectZone) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.zone!!.isAvailable
                    ) {
                        Text(
                            text = if (state.zone!!.isAvailable) "이 구역 선택하기" else "현재 이용 불가",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            state.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.errorMessage!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.processIntent(ZoneDetailContract.ZoneDetailIntent.LoadZone) }) {
                            Text("다시 시도")
                        }
                    }
                }
            }
        }
    }

    // 삭제 확인 다이얼로그
    DeleteConfirmDialog(
        visible = showDeleteDialog,
        message = "정말로 이 구역을 삭제하시겠습니까?",
        onConfirm = {
            showDeleteDialog = false
            viewModel.confirmDeleteZone(zoneId)
        },
        onDismiss = { showDeleteDialog = false }
    )
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
