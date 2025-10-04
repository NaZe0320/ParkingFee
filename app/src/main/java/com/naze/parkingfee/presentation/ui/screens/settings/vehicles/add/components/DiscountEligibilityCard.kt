package com.naze.parkingfee.presentation.ui.screens.settings.vehicles.add.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 할인 자격 토글 카드
 */
@Composable
fun DiscountEligibilityCard(
    compactCarEnabled: Boolean,
    nationalMeritEnabled: Boolean,
    disabledEnabled: Boolean,
    onCompactCarChange: (Boolean) -> Unit,
    onNationalMeritChange: (Boolean) -> Unit,
    onDisabledChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "할인 자격",
                style = MaterialTheme.typography.titleMedium
            )
            
            // 경차 할인
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "경차",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "경차 할인을 적용합니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = compactCarEnabled,
                    onCheckedChange = onCompactCarChange
                )
            }
            
            HorizontalDivider()
            
            // 국가유공자 할인 (향후 확장)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "국가유공자",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "국가유공자 할인을 적용합니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = nationalMeritEnabled,
                    onCheckedChange = onNationalMeritChange,
                    enabled = false // 향후 확장
                )
            }
            
            HorizontalDivider()
            
            // 장애인 할인 (향후 확장)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "장애인",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "장애인 할인을 적용합니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = disabledEnabled,
                    onCheckedChange = onDisabledChange,
                    enabled = false // 향후 확장
                )
            }
        }
    }
}
