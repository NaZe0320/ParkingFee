package com.naze.parkingfee.presentation.ui.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.naze.parkingfee.domain.model.ParkingAlarm

/**
 * 알람 설정 섹션 컴포넌트
 */
@Composable
fun AlarmSettingSection(
    parkingAlarms: List<ParkingAlarm>,
    onAddAlarm: (targetAmount: Double, minutesBefore: Int) -> Unit,
    onRemoveAlarm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var targetAmountText by remember { mutableStateOf("") }
    var minutesBeforeText by remember { mutableStateOf("") }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 제목
            Text(
                text = "알람 설정",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 목표 금액 입력
            OutlinedTextField(
                value = targetAmountText,
                onValueChange = { targetAmountText = it },
                label = { Text("목표 금액 (원)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // n분 전 입력
            OutlinedTextField(
                value = minutesBeforeText,
                onValueChange = { minutesBeforeText = it },
                label = { Text("몇 분 전 알림") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 설정 버튼
            Button(
                onClick = {
                    val targetAmount = targetAmountText.toDoubleOrNull()
                    val minutesBefore = minutesBeforeText.toIntOrNull()
                    
                    if (targetAmount != null && minutesBefore != null && 
                        targetAmount > 0 && minutesBefore > 0) {
                        onAddAlarm(targetAmount, minutesBefore)
                        targetAmountText = ""
                        minutesBeforeText = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = targetAmountText.isNotBlank() && minutesBeforeText.isNotBlank()
            ) {
                Text("알람 추가")
            }
            
            // 설정된 알람 리스트
            if (parkingAlarms.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text(
                    text = "설정된 알람",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                parkingAlarms.forEach { alarm ->
                    AlarmItem(
                        alarm = alarm,
                        onRemove = { onRemoveAlarm(alarm.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmItem(
    alarm: ParkingAlarm,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isTriggered) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${String.format("%.0f", alarm.targetAmount)}원",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (alarm.isTriggered) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = "${alarm.minutesBefore}분 전 알림${if (alarm.isTriggered) " (완료)" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (alarm.isTriggered) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "알람 삭제",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

