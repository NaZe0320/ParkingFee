package com.naze.parkingfee.presentation.ui.screens.settings.vehicles.add.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 차량 이름 입력 컴포넌트
 */
@Composable
fun VehicleNameInputCard(
    vehicleName: String,
    onNameChange: (String) -> Unit,
    nameError: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "차량 이름",
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedTextField(
                value = vehicleName,
                onValueChange = onNameChange,
                label = { Text("차량 이름 (선택사항)") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } }
            )
            
            Text(
                text = "차량을 구분하기 위한 이름입니다. 예: 내 차, 아내 차 등",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
