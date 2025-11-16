package com.naze.parkingfee.presentation.ui.screens.settings.vehicles.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.naze.parkingfee.infrastructure.notification.ToastManager
import com.naze.parkingfee.presentation.ui.screens.vehicles.list.components.VehicleItem

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
    val context = LocalContext.current
    
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
    
    // Effect 처리 - SharedFlow를 직접 collect하여 모든 Effect를 순차적으로 처리
    LaunchedEffect(Unit) {
        viewModel.effect.collect { currentEffect ->
            when (currentEffect) {
                is VehicleListContract.VehicleListEffect.ShowToast -> {
                    ToastManager.show(context, currentEffect.message)
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
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
        // 헤더와 추가 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "차량 관리",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "등록된 차량 ${state.vehicles.size}/3",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (state.canAddVehicle) {
                Button(
                    onClick = { viewModel.processIntent(VehicleListContract.VehicleListIntent.NavigateToAddVehicle) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "차량 추가", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("추가", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
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
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 0.dp,
                        start = 0.dp,
                        end = 0.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.vehicles) { vehicle ->
                        VehicleItem(
                            vehicle = vehicle,
                            isSelected = false,
                            onSelectClick = {
                                // 아이템 클릭 시 선택 대신 편집(상세) 화면으로 이동
                                viewModel.processIntent(
                                    VehicleListContract.VehicleListIntent.NavigateToEditVehicle(
                                        vehicle.id
                                    )
                                )
                            },
                            // 목록 화면에서는 편집/삭제 액션을 제공하지 않으므로 noop 처리
                            onEditClick = {},
                            onDeleteClick = {},
                            showMenuButton = false
                        )
                    }
                }
            }
        }
        
        // 에러 메시지 표시
        state.errorMessage?.let { errorMessage ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
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

