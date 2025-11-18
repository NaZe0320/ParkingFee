package com.naze.parkingfee.presentation.ui.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 무료 시간 선택 컴포넌트
 */
@Composable
fun FreeTimeSelector(
    freeTimeMinutes: Int,
    onAddFreeTime: (Int) -> Unit,
    onRemoveFreeTime: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
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
                text = "무료 시간",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 현재 무료 시간 표시
            Text(
                text = formatFreeTime(freeTimeMinutes),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // 10분 버튼
            FreeTimeButton(
                label = "10분",
                onAdd = { onAddFreeTime(10) },
                onRemove = { onRemoveFreeTime(10) },
                enabled = freeTimeMinutes >= 10
            )
            
            // 30분 버튼
            FreeTimeButton(
                label = "30분",
                onAdd = { onAddFreeTime(30) },
                onRemove = { onRemoveFreeTime(30) },
                enabled = freeTimeMinutes >= 30
            )
            
            // 1시간 버튼
            FreeTimeButton(
                label = "1시간",
                onAdd = { onAddFreeTime(60) },
                onRemove = { onRemoveFreeTime(60) },
                enabled = freeTimeMinutes >= 60
            )
        }
    }
}

@Composable
private fun FreeTimeButton(
    label: String,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 빼기 버튼
            FilledTonalIconButton(
                onClick = onRemove,
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "$label 빼기"
                )
            }
            
            // 더하기 버튼
            FilledTonalIconButton(
                onClick = onAdd
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "$label 더하기"
                )
            }
        }
    }
}

/**
 * 무료 시간을 포맷팅합니다.
 */
private fun formatFreeTime(minutes: Int): String {
    if (minutes == 0) return "0분"
    
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    
    return when {
        hours > 0 && remainingMinutes > 0 -> "${hours}시간 ${remainingMinutes}분"
        hours > 0 -> "${hours}시간"
        else -> "${remainingMinutes}분"
    }
}

