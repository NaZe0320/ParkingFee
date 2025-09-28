package com.naze.parkingfee.presentation.ui.screens.addparkinglot.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 주차장 기본 정보 입력 컴포넌트
 */
@Composable
fun ParkingLotBasicInfoCard(
    parkingLotName: String,
    useDefaultName: Boolean,
    onNameChange: (String) -> Unit,
    onUseDefaultNameChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    nameError: String? = null
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "주차장 정보",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 기본 이름 사용 여부
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "기본 이름 사용",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = useDefaultName,
                    onCheckedChange = onUseDefaultNameChange
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 주차장 이름 입력 (기본 이름 사용하지 않는 경우만)
            if (!useDefaultName) {
                OutlinedTextField(
                    value = parkingLotName,
                    onValueChange = onNameChange,
                    label = { Text("주차장 이름") },
                    placeholder = { Text("예: 강남역 주차장") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError != null,
                    supportingText = if (nameError != null) {
                        { Text(nameError, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
            } else {
                Text(
                    text = "기본 이름을 사용하면 '주차장1', '주차장2' 등으로 자동 설정됩니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
