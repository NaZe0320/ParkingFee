package com.naze.parkingfee.presentation.ui.screens.settings.parkinglots.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naze.parkingfee.domain.model.ParkingZone

/**
 * 주차장 목록 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingLotListScreen(
    viewModel: ParkingLotListViewModel = hiltViewModel(),
    onNavigateToAddParkingLot: () -> Unit = {},
    onNavigateToEditParkingLot: (zoneId: String) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle(initialValue = null)
    
    // ViewModel init에서 자동 로드되므로 별도 새로고침 불필요
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var parkingLotToDelete by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    // Effect 처리
    LaunchedEffect(effect) {
        effect?.let { currentEffect ->
            when (currentEffect) {
                is ParkingLotListContract.ParkingLotListEffect.ShowToast -> {
                    // Toast 표시 로직
                }
                is ParkingLotListContract.ParkingLotListEffect.NavigateToAddParkingLot -> {
                    onNavigateToAddParkingLot()
                }
                is ParkingLotListContract.ParkingLotListEffect.NavigateToEditParkingLot -> {
                    onNavigateToEditParkingLot(currentEffect.zoneId)
                }
                is ParkingLotListContract.ParkingLotListEffect.ShowDeleteConfirmation -> {
                    parkingLotToDelete = Pair(currentEffect.zoneId, currentEffect.zoneName)
                    showDeleteDialog = true
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 헤더와 추가 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "주차장 관리",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 정렬 버튼
                IconButton(
                    onClick = { showSortMenu = true }
                ) {
                    Icon(
                        Icons.Default.Sort,
                        contentDescription = "정렬",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // 추가 버튼
                Button(
                    onClick = { viewModel.processIntent(ParkingLotListContract.ParkingLotListIntent.NavigateToAddParkingLot) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "주차장 추가", modifier = Modifier.size(18.dp))
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
            // 주차장 목록
            if (state.parkingLots.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "등록된 주차장이 없습니다",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "주차장을 등록하여 요금 정보를 설정하세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.parkingLots) { parkingLot ->
                        ParkingLotItem(
                            parkingLot = parkingLot,
                            isSelected = state.selectedParkingLotId == parkingLot.id,
                            onSelectClick = {
                                viewModel.processIntent(ParkingLotListContract.ParkingLotListIntent.SelectParkingLot(parkingLot.id))
                            },
                            onEditClick = { 
                                viewModel.processIntent(ParkingLotListContract.ParkingLotListIntent.NavigateToEditParkingLot(parkingLot.id))
                            },
                            onDeleteClick = { 
                                viewModel.processIntent(ParkingLotListContract.ParkingLotListIntent.DeleteParkingLot(parkingLot.id))
                            },
                            onFavoriteClick = {
                                viewModel.processIntent(ParkingLotListContract.ParkingLotListIntent.ToggleFavorite(parkingLot.id))
                            }
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
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
    
    // 정렬 메뉴
    DropdownMenu(
        expanded = showSortMenu,
        onDismissRequest = { showSortMenu = false }
    ) {
        ParkingLotListContract.SortOrder.values().forEach { sortOrder ->
            DropdownMenuItem(
                text = { 
                    Text(
                        text = when (sortOrder) {
                            ParkingLotListContract.SortOrder.NAME -> "이름순"
                            ParkingLotListContract.SortOrder.RECENT -> "최근 사용순"
                            ParkingLotListContract.SortOrder.FAVORITE -> "즐겨찾기순"
                        }
                    )
                },
                onClick = {
                    viewModel.processIntent(ParkingLotListContract.ParkingLotListIntent.ChangeSortOrder(sortOrder))
                    showSortMenu = false
                },
                leadingIcon = {
                    if (state.sortOrder == sortOrder) {
                        Icon(Icons.Default.Check, contentDescription = "선택됨")
                    }
                }
            )
        }
    }
    
    // 삭제 확인 다이얼로그
    if (showDeleteDialog && parkingLotToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                parkingLotToDelete = null
            },
            title = { Text("주차장 삭제") },
            text = { Text("'${parkingLotToDelete?.second}' 주차장을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        parkingLotToDelete?.let { (zoneId, _) ->
                            viewModel.confirmDeleteParkingLot(zoneId)
                        }
                        showDeleteDialog = false
                        parkingLotToDelete = null
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        parkingLotToDelete = null
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }
}

/**
 * 주차장 아이템 컴포넌트 (선택 가능한 카드 스타일)
 */
@Composable
private fun ParkingLotItem(
    parkingLot: ParkingZone,
    isSelected: Boolean,
    onSelectClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
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
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 주차장 정보
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // 주차장 아이콘
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalParking,
                        contentDescription = "주차장 아이콘",
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // 주차장 정보
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = parkingLot.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = parkingLot.getDisplayFeeInfo(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    // 공영 주차장 배지
                    if (parkingLot.isPublic) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.tertiaryContainer
                                }
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "공영",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onTertiary
                                } else {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            
            // 선택 표시 및 액션 버튼들
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 선택 표시
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "선택됨",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 즐겨찾기 버튼
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        if (parkingLot.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (parkingLot.isFavorite) "즐겨찾기 해제" else "즐겨찾기",
                        tint = if (parkingLot.isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                // 더보기 메뉴 버튼
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "더보기",
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(18.dp)
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
}
