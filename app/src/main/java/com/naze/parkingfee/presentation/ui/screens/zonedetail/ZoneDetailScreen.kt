package com.naze.parkingfee.presentation.ui.screens.zonedetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
                    TextButton(onClick = { viewModel.processIntent(ZoneDetailContract.ZoneDetailIntent.NavigateToEdit) }) {
                        Text("편집")
                    }
                    TextButton(onClick = { viewModel.processIntent(ZoneDetailContract.ZoneDetailIntent.DeleteZone) }) {
                        Text("삭제")
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
                val zone = state.zone!!

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 구역 기본 정보 카드
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = zone.name,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (zone.isPublic) {
                                        AssistChip(
                                            onClick = { },
                                            label = {
                                                Text(
                                                    text = "공영",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            enabled = false,
                                            colors = AssistChipDefaults.assistChipColors(
                                                disabledContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                disabledLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 요금 체계 상세 정보 (복잡한 요금 체계가 있는 경우)
                    zone.feeStructure?.let { feeStructure ->
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "요금 체계",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    // 커스텀 요금 구간이 있으면 고급 모드로 표시
                                    if (feeStructure.customFeeRules.isNotEmpty()) {
                                        // 고급 모드: 커스텀 요금 구간을 시간대별로 표시
                                        feeStructure.customFeeRules.forEachIndexed { idx, rule ->
                                            val maxText = rule.maxMinutes?.let { "~${it}분" } ?: "이상"
                                            val timeRange = "${rule.minMinutes}분$maxText"
                                            val feeInfo = if (rule.isFixedFee) {
                                                "${rule.fee}원 (고정 요금)"
                                            } else {
                                                "${rule.fee}원 (${rule.unitMinutes}분마다)"
                                            }
                                            
                                            InfoRow(
                                                label = timeRange,
                                                value = feeInfo
                                            )
                                        }
                                    } else {
                                        // 단순 모드: 최초 요금, 기본 요금 표시
                                        InfoRow(
                                            "최초 요금",
                                            "${feeStructure.basicFee.fee}원 (${feeStructure.basicFee.durationMinutes}분)"
                                        )
                                        InfoRow(
                                            "기본 요금",
                                            "${feeStructure.additionalFee.fee}원 (${feeStructure.additionalFee.intervalMinutes}분마다)"
                                        )
                                    }

                                    feeStructure.dailyMaxFee?.let { dailyMax ->
                                        Spacer(modifier = Modifier.height(4.dp))
                                        InfoRow("일 최대 요금", "${dailyMax.maxFee}원")
                                    }
                                }
                            }
                        }
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
    if (state.zone != null) {
        DeleteConfirmDialog(
            visible = showDeleteDialog,
            title = "주차장 삭제",
            itemName = state.zone!!.name,
            message = "이 주차장을 삭제하시겠습니까?",
            onConfirm = {
                showDeleteDialog = false
                viewModel.confirmDeleteZone(zoneId)
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
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
