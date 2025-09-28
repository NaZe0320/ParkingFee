package com.naze.parkingfee.presentation.ui.screens.addparkinglot.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 추가 요금 체계 입력 컴포넌트
 */
@Composable
fun AdditionalFeeRuleCard(
    intervalMinutes: Int,
    feeAmount: Int,
    onIntervalChange: (Int) -> Unit,
    onFeeChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    intervalError: String? = null,
    feeError: String? = null
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "추가 요금 체계",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "초과 시 매 N분당 M원",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 간격 입력
                OutlinedTextField(
                    value = intervalMinutes.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { onIntervalChange(it) }
                    },
                    label = { Text("간격 (분)") },
                    modifier = Modifier.weight(1f),
                    isError = intervalError != null,
                    supportingText = if (intervalError != null) {
                        { Text(intervalError, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
                
                // 요금 입력
                OutlinedTextField(
                    value = feeAmount.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { onFeeChange(it) }
                    },
                    label = { Text("요금 (원)") },
                    modifier = Modifier.weight(1f),
                    isError = feeError != null,
                    supportingText = if (feeError != null) {
                        { Text(feeError, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
            }
        }
    }
}
