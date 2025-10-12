package com.naze.parkingfee.presentation.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naze.parkingfee.domain.model.vehicle.Vehicle
import com.naze.parkingfee.presentation.ui.screens.home.components.VehicleItem

/**
 * 차량 선택 컴포넌트
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
                .padding(horizontal = 4.dp, vertical = 8.dp),
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
                
                // 접혔을 때 선택된 차량 정보 표시
                if (!isExpanded && selectedVehicle != null) {
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
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                vehicles.forEach { vehicle ->
                    VehicleItem(
                        vehicle = vehicle,
                        isSelected = selectedVehicle?.id == vehicle.id,
                        onVehicleSelected = onVehicleSelected
                    )
                }
            }
        }
    }
}

