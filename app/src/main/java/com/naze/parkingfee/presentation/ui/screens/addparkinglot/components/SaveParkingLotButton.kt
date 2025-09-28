package com.naze.parkingfee.presentation.ui.screens.addparkinglot.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 주차장 저장 버튼 컴포넌트
 */
@Composable
fun SaveParkingLotButton(
    isSaving: Boolean,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onSaveClick,
        enabled = !isSaving,
        modifier = modifier.fillMaxWidth()
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("저장 중...")
        } else {
            Text(
                text = "주차장 저장",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
