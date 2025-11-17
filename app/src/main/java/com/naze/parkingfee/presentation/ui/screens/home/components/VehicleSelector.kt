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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naze.parkingfee.domain.model.vehicle.Vehicle

/**
 * 차량 선택 컴포넌트 (홈 화면용)
 * - 최대 3개의 차량을 가로 스크롤로 표시
 * - 선택된 차량을 항상 앞에 노출
 */
@Composable
fun VehicleSelector(
    vehicles: List<Vehicle>,
    selectedVehicle: Vehicle?,
    onVehicleSelected: (Vehicle) -> Unit,
    isExpanded: Boolean = true,
    onToggleExpand: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Title 행 - 접기/펼치기 아이콘과 선택된 차량 정보
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
                    text = "차량",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 선택된 차량 정보 표시 (항상 표시)
                if (selectedVehicle != null) {
                    Text(
                        text = if (selectedVehicle.hasPlateNumber) {
                            "${selectedVehicle.displayName}(${selectedVehicle.displayPlateNumber})"
                        } else {
                            selectedVehicle.displayName
                        },
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

        // 펼쳤을 때만 차량 목록 표시
        if (isExpanded) {
            Spacer(modifier = Modifier.height(8.dp))

            if (vehicles.isEmpty()) {
                Text(
                    text = "등록된 차량이 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            } else {
                val selectedId = selectedVehicle?.id
                val sortedVehicles: List<Vehicle> = remember(vehicles, selectedId) {
                    vehicles.sortedWith(
                        compareByDescending<Vehicle> { it.id == selectedId }
                            .thenBy { it.displayName }
                    )
                }
                val displayVehicles = sortedVehicles.take(3)

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(displayVehicles) { vehicle ->
                        VehicleChip(
                            vehicle = vehicle,
                            isSelected = selectedVehicle?.id == vehicle.id,
                            onClick = { onVehicleSelected(vehicle) }
                        )
                    }
                }
            }
        }
    }
}
