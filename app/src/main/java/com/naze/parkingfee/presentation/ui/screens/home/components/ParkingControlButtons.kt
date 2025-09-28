package com.naze.parkingfee.presentation.ui.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naze.parkingfee.domain.model.ParkingZone

/**
 * 주차 시작/종료 버튼 컴포넌트
 */
@Composable
fun ParkingControlButtons(
    isParkingActive: Boolean,
    selectedZone: ParkingZone?,
    onStartParking: (String) -> Unit,
    onStopParking: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!isParkingActive) {
            Button(
                onClick = { 
                    selectedZone?.let { zone ->
                        onStartParking(zone.id)
                    }
                },
                enabled = selectedZone != null,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "주차 시작",
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Button(
                onClick = { 
                    // 활성 세션의 ID를 전달해야 함
                    onStopParking("session_id")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "주차 종료",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
        
        OutlinedButton(
            onClick = { /* 새로고침 로직 */ },
            modifier = Modifier.weight(1f)
        ) {
            Text("새로고침")
        }
    }
}
