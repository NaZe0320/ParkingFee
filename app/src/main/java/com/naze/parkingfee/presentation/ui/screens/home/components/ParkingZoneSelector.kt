package com.naze.parkingfee.presentation.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
 * 주차장 선택 컴포넌트
 * React 디자인의 주차장 선택 UI를 구현합니다.
 */
@Composable
fun ParkingZoneSelector(
    zones: List<ParkingZone>,
    selectedZone: ParkingZone?,
    onZoneSelected: (ParkingZone) -> Unit,
    onRequestZoneAction: (ParkingZone, ZoneAction) -> Unit,
    isExpanded: Boolean = true,
    onToggleExpand: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Title 행 - 접기/펼치기 아이콘과 선택된 주차장 정보
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "주차장",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // 접혔을 때 선택된 주차장 정보 표시
                if (!isExpanded && selectedZone != null) {
                    Text(
                        text = selectedZone.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "접기" else "펼치기",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // 펼쳤을 때만 주차장 목록 표시
        if (isExpanded) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                zones.forEach { zone ->
                    ZoneItem(
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
private fun ZoneItem(
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
        } else {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 주차장 아이콘
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalParking,
                        contentDescription = "주차장",
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // 주차장 정보
                Column {
                    Text(
                        text = zone.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = zone.getDisplayFeeInfo(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 공영 주차장 배지
                if (zone.isPublic) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "공영",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                // 선택 표시
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "선택됨",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
    
    // 더보기 메뉴
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
