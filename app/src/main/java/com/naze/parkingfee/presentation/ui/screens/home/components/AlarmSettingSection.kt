package com.naze.parkingfee.presentation.ui.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 제목과 아이콘
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "알람 설정",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(14.dp)
                    )
                }
                
                Text(
                    text = "알람 설정",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // 입력 필드들을 한 줄에 컴팩트하게
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 목표 금액 입력
                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it },
                    placeholder = { Text("금액", style = MaterialTheme.typography.bodySmall) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).height(48.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                
                // n분 전 입력
                OutlinedTextField(
                    value = minutesBeforeText,
                    onValueChange = { minutesBeforeText = it },
                    placeholder = { Text("n분 전", style = MaterialTheme.typography.bodySmall) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.8f).height(48.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                
                // 추가 버튼
                FilledTonalButton(
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
                    enabled = targetAmountText.isNotBlank() && minutesBeforeText.isNotBlank(),
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Text(
                        "추가",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // 설정된 알람 리스트
            if (parkingAlarms.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
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
                MaterialTheme.colorScheme.secondaryContainer
            }
        ),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${String.format("%.0f", alarm.targetAmount)}원",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (alarm.isTriggered) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
                Text(
                    text = "·",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (alarm.isTriggered) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                    }
                )
                Text(
                    text = "${alarm.minutesBefore}분 전${if (alarm.isTriggered) " (완료)" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (alarm.isTriggered) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    }
                )
            }
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "알람 삭제",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

