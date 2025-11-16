package com.naze.parkingfee.presentation.ui.screens.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naze.parkingfee.domain.model.ParkingZone

@Composable
fun ParkingZoneSelectDialog(
    visible: Boolean,
    zones: List<ParkingZone>,
    onSelectZone: (ParkingZone) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
            searchQuery = ""
        },
        title = {
            Text(
                text = "주차장 선택",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("주차장 검색") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                val filteredZones = remember(zones, searchQuery) {
                    if (searchQuery.isBlank()) zones
                    else zones.filter { it.name.contains(searchQuery, ignoreCase = true) }
                }

                if (filteredZones.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = "검색 결과가 없습니다",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                    ) {
                        items(filteredZones) { zone ->
                            androidx.compose.material3.ListItem(
                                headlineContent = {
                                    Text(
                                        text = zone.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        text = zone.getDisplayFeeInfo(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onSelectZone(zone)
                                        searchQuery = ""
                                        onDismiss()
                                    }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    searchQuery = ""
                    onDismiss()
                }
            ) {
                Text("닫기")
            }
        }
    )
}


