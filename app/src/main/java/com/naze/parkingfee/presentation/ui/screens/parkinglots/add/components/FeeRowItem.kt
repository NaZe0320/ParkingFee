package com.naze.parkingfee.presentation.ui.screens.parkinglots.add.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.naze.parkingfee.presentation.ui.screens.parkinglots.add.AddParkingLotContract.FeeRow

/**
 * 고급 모드용 요금 구간 행 컴포넌트
 */
@Composable
fun FeeRowItem(
    index: Int,
    row: FeeRow,
    isLastItem: Boolean,
    onUpdate: (startTime: Int?, endTime: Int?, unitMinutes: Int?, unitFee: Int?, isFixedFee: Boolean?) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 상단: 시간 구간 입력 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 시작 시간 입력
                if (index == 0) {
                    // 첫 번째 행은 시작 시간이 0으로 고정
                    OutlinedTextField(
                        value = "0분",
                        onValueChange = { },
                        label = { Text("시작") },
                        modifier = Modifier.weight(1f),
                        enabled = false,
                        readOnly = true,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                } else {
                    // 시작 시간 입력
                    var startTimeText by remember(row.startTime) { 
                        mutableStateOf(row.startTime.toString()) 
                    }
                    
                    OutlinedTextField(
                        value = startTimeText,
                        onValueChange = { newValue ->
                            startTimeText = newValue
                            val value = newValue.toIntOrNull()
                            if (value != null && value >= 0) {
                                // 이전 행의 endTime과 비교하여 유효성 검사
                                onUpdate(value, null, null, null, null)
                            }
                        },
                        label = { Text("시작") },
                        placeholder = { Text("분") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        suffix = { 
                            Text(
                                text = "분",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
                
                Text(
                    text = "~",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                // 종료 시간
                if (isLastItem) {
                    // 마지막 행은 종료 시간 수정 불가 (무제한)
                    OutlinedTextField(
                        value = "계속",
                        onValueChange = { },
                        label = { Text("종료") },
                        modifier = Modifier.weight(1f),
                        enabled = false,
                        readOnly = true,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.primary,
                            disabledBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )
                } else {
                    // 종료 시간 입력
                    var endTimeText by remember(row.endTime) { 
                        mutableStateOf(row.endTime?.toString() ?: "") 
                    }
                    
                    OutlinedTextField(
                        value = endTimeText,
                        onValueChange = { newValue ->
                            endTimeText = newValue
                            val value = newValue.toIntOrNull()
                            if (value != null && value > row.startTime) {
                                onUpdate(null, value, null, null, null)
                            }
                        },
                        label = { Text("종료") },
                        placeholder = { Text("분") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        suffix = { 
                            Text(
                                text = "분",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        isError = endTimeText.toIntOrNull()?.let { it <= row.startTime } ?: false
                    )
                }
                
                // 삭제 버튼 (첫 행, 마지막 행 제외)
                if (index > 0 && !isLastItem) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "삭제",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // 하단: 요금 설정 (단위 시간, 금액)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 단위 시간 입력
                    var unitMinutesText by remember(row.unitMinutes) { 
                        mutableStateOf(row.unitMinutes.toString()) 
                    }
                    
                    OutlinedTextField(
                        value = unitMinutesText,
                        onValueChange = { newValue ->
                            unitMinutesText = newValue
                            val value = newValue.toIntOrNull()
                            if (value != null && value > 0) {
                                onUpdate(null, null, value, null, null)
                            } else if (newValue.isEmpty()) {
                                // 빈 값은 허용 (입력 중일 수 있음)
                                unitMinutesText = ""
                            }
                        },
                        label = { Text("단위(분)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        supportingText = { Text(if (row.isFixedFee) "고정 요금" else "매 N분당") },
                        enabled = !row.isFixedFee, // 고정 요금이면 비활성화
                        isError = unitMinutesText.toIntOrNull()?.let { it <= 0 } ?: false
                    )
                    
                    // 요금 입력
                    var unitFeeText by remember(row.unitFee) { 
                        mutableStateOf(row.unitFee.toString()) 
                    }
                    
                    OutlinedTextField(
                        value = unitFeeText,
                        onValueChange = { newValue ->
                            unitFeeText = newValue
                            val value = newValue.toIntOrNull()
                            if (value != null && value >= 0) {
                                onUpdate(null, null, null, value, null)
                            }
                        },
                        label = { Text("요금(원)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        supportingText = { Text(if (row.isFixedFee) "고정 요금" else "부과 요금") }
                    )
                }
                
                // 고정 요금 체크박스
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = row.isFixedFee,
                        onCheckedChange = { checked ->
                            onUpdate(null, null, null, null, checked)
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "고정 요금 (구간 전체에 일괄 적용)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

