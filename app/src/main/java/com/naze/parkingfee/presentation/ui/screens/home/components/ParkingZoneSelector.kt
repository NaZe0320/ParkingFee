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
import com.naze.parkingfee.presentation.ui.screens.home.components.ZoneAction
import com.naze.parkingfee.presentation.ui.screens.parkinglots.list.components.ParkingLotItem


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
                val sortedZones = zones.sortedWith(
                    compareByDescending<ParkingZone> { it.isFavorite }
                        .thenBy { it.name }
                )
                sortedZones.forEach { zone ->
                    ParkingLotItem(
                        parkingLot = zone,
                        isSelected = selectedZone?.id == zone.id,
                        onSelectClick = { onZoneSelected(zone) },
                        onEditClick = { onRequestZoneAction(zone, ZoneAction.Edit) },
                        onDeleteClick = { onRequestZoneAction(zone, ZoneAction.Delete) },
                        onDetailClick = { onRequestZoneAction(zone, ZoneAction.Detail) },
                        onFavoriteClick = {}, // 홈 화면에서는 사용 안함
                        showFavoriteButton = false, // 즐겨찾기 버튼 숨김
                        showMenuButton = true // 더보기 버튼 표시
                    )
                }
            }
        }
    }
}

