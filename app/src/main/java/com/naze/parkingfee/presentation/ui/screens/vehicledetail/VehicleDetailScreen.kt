package com.naze.parkingfee.presentation.ui.screens.vehicledetail

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
 * 차량 상세 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    vehicleId: String,
    viewModel: VehicleDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 차량 ID 설정
    LaunchedEffect(vehicleId) {
        viewModel.setVehicleId(vehicleId)
    }

    // Effect 처리 - SharedFlow를 직접 collect하여 모든 Effect를 순차적으로 처리
    LaunchedEffect(Unit) {
        viewModel.effect.collect { currentEffect ->
            when (currentEffect) {
                is VehicleDetailContract.VehicleDetailEffect.ShowToast -> {
                    ToastManager.show(context, currentEffect.message)
                }
                is VehicleDetailContract.VehicleDetailEffect.NavigateToEdit -> {
                    onNavigateToEdit(currentEffect.vehicleId)
                }
                is VehicleDetailContract.VehicleDetailEffect.NavigateBack -> {
                    onNavigateBack()
                }
                is VehicleDetailContract.VehicleDetailEffect.ShowDeleteConfirmDialog -> {
                    showDeleteDialog = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("차량 상세") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.processIntent(VehicleDetailContract.VehicleDetailIntent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.processIntent(VehicleDetailContract.VehicleDetailIntent.NavigateToEdit) }) {
                        Text("편집")
                    }
                    TextButton(onClick = { viewModel.processIntent(VehicleDetailContract.VehicleDetailIntent.DeleteVehicle) }) {
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
            state.vehicle != null -> {
                val vehicle = state.vehicle!!

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
                    // 차량 기본 정보 카드
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
                                // 차량 이름
                                Text(
                                    text = vehicle.displayName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                HorizontalDivider(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                )

                                // 번호판
                                InfoRow("번호판", vehicle.displayPlateNumber)
                                
                                // 차량 이름 (번호판과 다른 경우에만 표시)
                                if (vehicle.name != null && vehicle.name != vehicle.plateNumber) {
                                    InfoRow("차량 이름", vehicle.name)
                                }
                            }
                        }
                    }

                    // 할인 자격 카드
                    if (vehicle.hasDiscount) {
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
                                        text = "할인 자격",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    // 경차 할인
                                    if (vehicle.discountEligibilities.compactCar.enabled) {
                                        DiscountChip("경차")
                                    }
                                    
                                    // 저공해 차 할인
                                    if (vehicle.discountEligibilities.lowEmission.enabled) {
                                        DiscountChip("저공해 차")
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
                        Button(onClick = { viewModel.processIntent(VehicleDetailContract.VehicleDetailIntent.LoadVehicle) }) {
                            Text("다시 시도")
                        }
                    }
                }
            }
        }
    }

    // 삭제 확인 다이얼로그
    if (state.vehicle != null) {
        DeleteConfirmDialog(
            visible = showDeleteDialog,
            title = "차량 삭제",
            itemName = state.vehicle!!.displayName,
            message = "이 차량을 삭제하시겠습니까?",
            onConfirm = {
                showDeleteDialog = false
                viewModel.confirmDeleteVehicle(vehicleId)
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

@Composable
private fun DiscountChip(
    label: String,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = { },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        },
        enabled = false,
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = modifier
    )
}

