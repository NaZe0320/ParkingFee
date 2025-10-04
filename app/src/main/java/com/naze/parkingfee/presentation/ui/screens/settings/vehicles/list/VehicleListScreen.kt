package com.naze.parkingfee.presentation.ui.screens.settings.vehicles.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * 차량 목록 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleListScreen(
    viewModel: VehicleListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToAddVehicle: () -> Unit = {},
    onNavigateToEditVehicle: (vehicleId: String) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle(initialValue = null)
    
    // 화면이 다시 포커스될 때 자동 새로고침
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshVehicles()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var vehicleToDelete by remember { mutableStateOf<Pair<String, String>?>(null) }
    
    // Effect 처리
    LaunchedEffect(effect) {
        effect?.let { currentEffect ->
            when (currentEffect) {
                is VehicleListContract.VehicleListEffect.ShowToast -> {
                    // Toast 표시 로직
                }
                is VehicleListContract.VehicleListEffect.NavigateBack -> {
                    onNavigateBack()
                }
                is VehicleListContract.VehicleListEffect.NavigateToAddVehicle -> {
                    onNavigateToAddVehicle()
                }
                is VehicleListContract.VehicleListEffect.NavigateToEditVehicle -> {
                    onNavigateToEditVehicle(currentEffect.vehicleId)
                }
                is VehicleListContract.VehicleListEffect.ShowDeleteConfirmation -> {
                    vehicleToDelete = Pair(currentEffect.vehicleId, currentEffect.vehicleName)
                    showDeleteDialog = true
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("차량 관리") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.processIntent(VehicleListContract.VehicleListIntent.NavigateBack) }) {
                        Text("←")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.canAddVehicle) {
                FloatingActionButton(
                    onClick = { viewModel.processIntent(VehicleListContract.VehicleListIntent.NavigateToAddVehicle) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "차량 추가")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 상태 표시
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // 차량 목록
                if (state.vehicles.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "등록된 차량이 없습니다",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "차량을 등록하여 할인 정보를 설정하세요",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (state.canAddVehicle) {
                                Button(
                                    onClick = { viewModel.processIntent(VehicleListContract.VehicleListIntent.NavigateToAddVehicle) }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("차량 추가")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.vehicles) { vehicle ->
                            VehicleItem(
                                vehicle = vehicle,
                                isSelected = state.selectedVehicleId == vehicle.id,
                                onSelectClick = { 
                                    viewModel.processIntent(VehicleListContract.VehicleListIntent.SelectVehicle(vehicle.id))
                                },
                                onEditClick = { 
                                    viewModel.processIntent(VehicleListContract.VehicleListIntent.NavigateToEditVehicle(vehicle.id))
                                },
                                onDeleteClick = { 
                                    viewModel.processIntent(VehicleListContract.VehicleListIntent.DeleteVehicle(vehicle.id))
                                }
                            )
                        }
                    }
                }
            }
            
            // 에러 메시지 표시
            state.errorMessage?.let { errorMessage ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
    
    // 삭제 확인 다이얼로그
    if (showDeleteDialog && vehicleToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                vehicleToDelete = null
            },
            title = { Text("차량 삭제") },
            text = { Text("'${vehicleToDelete?.second}' 차량을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vehicleToDelete?.let { (vehicleId, _) ->
                            viewModel.confirmDeleteVehicle(vehicleId)
                        }
                        showDeleteDialog = false
                        vehicleToDelete = null
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        vehicleToDelete = null
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }
}

/**
 * 차량 아이템 컴포넌트 (선택 가능한 카드 스타일)
 */
@Composable
private fun VehicleItem(
    vehicle: com.naze.parkingfee.domain.model.vehicle.Vehicle,
    isSelected: Boolean,
    onSelectClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 라디오 버튼
            RadioButton(
                selected = isSelected,
                onClick = onSelectClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 차량 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = vehicle.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (vehicle.hasPlateNumber) {
                    Text(
                        text = vehicle.displayPlateNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                if (vehicle.hasDiscount) {
                    Row {
                        Text(
                            text = "할인 적용: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                        Text(
                            text = vehicle.discountEligibilities.activeEligibilities.joinToString(" / ") { it.typeName },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
            }
            
            // 더보기 메뉴 버튼
            Box {
                IconButton(
                    onClick = { showMenu = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "더보기",
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("편집") },
                        onClick = {
                            showMenu = false
                            onEditClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("삭제") },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}
