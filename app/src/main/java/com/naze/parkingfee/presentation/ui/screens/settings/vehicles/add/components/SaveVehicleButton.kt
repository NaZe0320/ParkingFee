package com.naze.parkingfee.presentation.ui.screens.settings.vehicles.add.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 저장 버튼 컴포넌트
 */
@Composable
fun SaveVehicleButton(
    isSaving: Boolean,
    onSaveClick: () -> Unit
) {
    Button(
        onClick = onSaveClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isSaving
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(if (isSaving) "저장 중..." else "저장")
    }
}
