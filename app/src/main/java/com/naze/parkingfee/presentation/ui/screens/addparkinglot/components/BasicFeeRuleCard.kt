package com.naze.parkingfee.presentation.ui.screens.addparkinglot.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 기본 요금 체계 입력 컴포넌트
 */
@Composable
fun BasicFeeRuleCard(
    durationMinutes: Int,
    feeAmount: Int,
    onDurationChange: (Int) -> Unit,
    onFeeChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    durationError: String? = null,
    feeError: String? = null
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "기본 요금 체계",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "처음 N분까지 M원",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 시간 입력
                OutlinedTextField(
                    value = durationMinutes.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { onDurationChange(it) }
                    },
                    label = { Text("시간 (분)") },
                    modifier = Modifier.weight(1f),
                    isError = durationError != null,
                    supportingText = if (durationError != null) {
                        { Text(durationError, color = MaterialTheme.colorScheme.error) }
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
