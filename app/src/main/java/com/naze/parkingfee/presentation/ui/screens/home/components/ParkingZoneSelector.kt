package com.naze.parkingfee.presentation.ui.screens.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naze.parkingfee.domain.model.ParkingZone

/**
 * 주차장 선택 컴포넌트 (홈 화면용)
 * - 최대 5개의 주차장을 가로 스크롤로 표시
 * - 선택된 주차장을 항상 앞에 노출
 * - 더보기 버튼을 통해 Dialog에서 검색 및 선택
 */
@Composable
fun ParkingZoneSelector(
    zones: List<ParkingZone>,
    selectedZone: ParkingZone?,
    onZoneSelected: (ParkingZone) -> Unit,
    isExpanded: Boolean = true,
    onToggleExpand: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMoreDialog by remember { mutableStateOf(false) }
    var lastDisplayedZoneIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Title 행 - 접기/펼치기 아이콘과 선택된 주차장 정보
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
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

                // 선택된 주차장 정보 표시 (항상 표시)
                if (selectedZone != null) {
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

            if (zones.isEmpty()) {
                Text(
                    text = "등록된 주차장이 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            } else {
                val selectedId = selectedZone?.id
                
                // 표시할 주차장 결정: Dialog에서 선택되었는지 체크
                val displayZones = remember(zones, selectedId, lastDisplayedZoneIds) {
                    val firstFiveZones = zones.take(5)
                    val firstFiveIds = firstFiveZones.map { it.id }.toSet()
                    
                    // Dialog에서 선택된 경우: 선택된 주차장이 기존 5개에 없었다면
                    if (selectedId != null && selectedId !in firstFiveIds && selectedId in zones.map { it.id }) {
                        val selectedZoneObj = zones.find { it.id == selectedId }
                        if (selectedZoneObj != null) {
                            // 선택된 주차장을 맨 앞에, 나머지 4개를 뒤에
                            listOf(selectedZoneObj) + zones.filter { it.id != selectedId }.take(4)
                        } else {
                            firstFiveZones
                        }
                    } else {
                        // 기존 5개 유지
                        firstFiveZones
                    }
                }
                
                // 현재 표시된 주차장 ID 업데이트
                LaunchedEffect(displayZones) {
                    lastDisplayedZoneIds = displayZones.map { it.id }.toSet()
                }
                
                val remainingCount = (zones.size - displayZones.size).coerceAtLeast(0)

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(displayZones) { zone ->
                        ParkingZoneChip(
                            zone = zone,
                            isSelected = selectedZone?.id == zone.id,
                            onClick = { onZoneSelected(zone) }
                        )
                    }

                    if (remainingCount > 0) {
                        item {
                            MoreParkingZoneChip(
                                count = remainingCount,
                                onClick = { showMoreDialog = true }
                            )
                        }
                    }
                }
            }
        }
    }

    ParkingZoneSelectDialog(
        visible = showMoreDialog,
        zones = zones,
        onSelectZone = { onZoneSelected(it) },
        onDismiss = { showMoreDialog = false }
    )
}
