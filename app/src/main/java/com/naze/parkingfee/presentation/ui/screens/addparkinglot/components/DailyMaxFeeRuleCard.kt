package com.naze.parkingfee.presentation.ui.screens.addparkinglot.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 일 최대 요금 체계 입력 컴포넌트
 */
@Composable
fun DailyMaxFeeRuleCard(
    enabled: Boolean,
    maxFeeAmount: Int,
    onEnabledChange: (Boolean) -> Unit,
    onFeeChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    feeError: String? = null
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "일 최대 요금",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "일 최대 N원 (장시간 주차 시 필수)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange
                )
            }
            
            if (enabled) {
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = maxFeeAmount.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { onFeeChange(it) }
                    },
                    label = { Text("일 최대 요금 (원)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = feeError != null,
                    supportingText = if (feeError != null) {
                        { Text(feeError, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
            }
        }
    }
}
