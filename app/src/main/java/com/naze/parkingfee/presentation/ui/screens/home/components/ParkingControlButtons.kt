package com.naze.parkingfee.presentation.ui.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.domain.model.ParkingSession

/**
 * 주차 시작/종료 버튼 컴포넌트
 * React 디자인의 버튼 스타일을 구현합니다.
 */
@Composable
fun ParkingControlButtons(
    isParkingActive: Boolean,
    selectedZone: ParkingZone?,
    activeSession: ParkingSession?,
    onStartParking: (String) -> Unit,
    onStopParking: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 주차 시작/종료 버튼
        Button(
            onClick = { 
                if (!isParkingActive) {
                    selectedZone?.let { zone ->
                        onStartParking(zone.id)
                    }
                } else {
                    activeSession?.let { session ->
                        onStopParking(session.id)
                    }
                }
            },
            enabled = if (!isParkingActive) selectedZone != null else activeSession != null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isParkingActive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = if (isParkingActive) 8.dp else 8.dp
            )
        ) {
            Text(
                text = if (isParkingActive) "종료" else "시작",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                color = if (isParkingActive) {
                    MaterialTheme.colorScheme.onError
                } else {
                    MaterialTheme.colorScheme.onPrimary
                },
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
