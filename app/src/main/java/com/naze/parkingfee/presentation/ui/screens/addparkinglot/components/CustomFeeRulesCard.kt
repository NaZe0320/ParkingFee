package com.naze.parkingfee.presentation.ui.screens.addparkinglot.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naze.parkingfee.domain.model.CustomFeeRule

/**
 * 커스텀 요금 구간 관리 컴포넌트
 */
@Composable
fun CustomFeeRulesCard(
    customFeeRules: List<CustomFeeRule>,
    onAddRule: () -> Unit,
    onRemoveRule: (Int) -> Unit,
    onUpdateRule: (Int, Int, Int?, Int) -> Unit,
    modifier: Modifier = Modifier,
    validationErrors: Map<String, String> = emptyMap()
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
                        text = "추가 요금 구간",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "복잡한 요금 구간을 추가로 설정할 수 있습니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = onAddRule) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "요금 구간 추가"
                    )
                }
            }
            
            if (customFeeRules.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    customFeeRules.forEachIndexed { index, rule ->
                        CustomFeeRuleItem(
                            rule = rule,
                            index = index,
                            onRemove = { onRemoveRule(index) },
                            onUpdate = { minMinutes, maxMinutes, fee ->
                                onUpdateRule(index, minMinutes, maxMinutes, fee)
                            },
                            validationErrors = validationErrors
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "추가 요금 구간이 없습니다. + 버튼을 눌러 추가하세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CustomFeeRuleItem(
    rule: CustomFeeRule,
    index: Int,
    onRemove: () -> Unit,
    onUpdate: (Int, Int?, Int) -> Unit,
    validationErrors: Map<String, String>
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "구간 ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "구간 삭제",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 최소 시간
                OutlinedTextField(
                    value = rule.minMinutes.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { minMinutes ->
                            onUpdate(minMinutes, rule.maxMinutes, rule.fee)
                        }
                    },
                    label = { Text("최소 (분)") },
                    modifier = Modifier.weight(1f),
                    isError = validationErrors.containsKey("customFeeRule_${index}_minMinutes"),
                    supportingText = validationErrors["customFeeRule_${index}_minMinutes"]?.let { error ->
                        { Text(error, color = MaterialTheme.colorScheme.error) }
                    }
                )
                
                // 최대 시간
                OutlinedTextField(
                    value = rule.maxMinutes?.toString() ?: "",
                    onValueChange = { value ->
                        val maxMinutes = if (value.isBlank()) null else value.toIntOrNull()
                        onUpdate(rule.minMinutes, maxMinutes, rule.fee)
                    },
                    label = { Text("최대 (분)") },
                    placeholder = { Text("무제한") },
                    modifier = Modifier.weight(1f),
                    isError = validationErrors.containsKey("customFeeRule_${index}_maxMinutes"),
                    supportingText = validationErrors["customFeeRule_${index}_maxMinutes"]?.let { error ->
                        { Text(error, color = MaterialTheme.colorScheme.error) }
                    }
                )
                
                // 요금
                OutlinedTextField(
                    value = rule.fee.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { fee ->
                            onUpdate(rule.minMinutes, rule.maxMinutes, fee)
                        }
                    },
                    label = { Text("요금 (원)") },
                    modifier = Modifier.weight(1f),
                    isError = validationErrors.containsKey("customFeeRule_${index}_fee"),
                    supportingText = validationErrors["customFeeRule_${index}_fee"]?.let { error ->
                        { Text(error, color = MaterialTheme.colorScheme.error) }
                    }
                )
            }
        }
    }
}
