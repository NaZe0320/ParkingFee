package com.naze.parkingfee.presentation.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naze.parkingfee.domain.model.ParkingZone

/**
 * 주차 구역 액션 타입
 */
enum class ZoneAction {
    Detail, Edit, Delete
}

/**
 * 주차 구역 선택 컴포넌트
 */
@Composable
fun ParkingZoneSelector(
    zones: List<ParkingZone>,
    selectedZone: ParkingZone?,
    onZoneSelected: (ParkingZone) -> Unit,
    onRequestZoneAction: (ParkingZone, ZoneAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "주차 구역 선택",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(zones) { zone ->
                    ZoneListItem(
                        zone = zone,
                        isSelected = selectedZone?.id == zone.id,
                        onZoneSelected = onZoneSelected,
                        onRequestZoneAction = onRequestZoneAction
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoneListItem(
    zone: ParkingZone,
    isSelected: Boolean,
    onZoneSelected: (ParkingZone) -> Unit,
    onRequestZoneAction: (ParkingZone, ZoneAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onZoneSelected(zone) },
                onLongClick = { showMenu = true }
            ),
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
        } else null
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
                onClick = { onZoneSelected(zone) },
                colors = RadioButtonDefaults.colors(
                    selectedColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 존 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = zone.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                Text(
                    text = zone.getDisplayFeeInfo(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
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
                        text = { Text("상세") },
                        onClick = {
                            showMenu = false
                            onRequestZoneAction(zone, ZoneAction.Detail)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("편집") },
                        onClick = {
                            showMenu = false
                            onRequestZoneAction(zone, ZoneAction.Edit)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("삭제") },
                        onClick = {
                            showMenu = false
                            onRequestZoneAction(zone, ZoneAction.Delete)
                        }
                    )
                }
            }
        }
    }
}
