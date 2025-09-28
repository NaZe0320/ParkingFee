package com.naze.parkingfee.presentation.ui.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naze.parkingfee.domain.model.ParkingSession

/**
 * 주차 상태 카드 컴포넌트
 */
@Composable
fun ParkingStatusCard(
    isActive: Boolean,
    session: ParkingSession?,
    duration: String,
    fee: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isActive) "주차 중" else "주차 대기",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isActive) {
                    MaterialTheme.colorScheme.onSecondary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isActive && session != null) {
                Text(
                    text = "시작 시간: ${formatTime(session.startTime)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "경과 시간: $duration",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "요금: ${String.format("%.0f", fee)}원",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            } else {
                Text(
                    text = "주차 구역을 선택하고 시작하세요",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    // 타임스탬프를 시간 형식으로 변환하는 로직
    return "12:34" // 임시 구현
}
