package com.naze.parkingfee.presentation.ui.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.naze.parkingfee.domain.model.ParkingSession
import com.naze.parkingfee.utils.TimeUtils
import com.naze.parkingfee.utils.FeeResult

/**
 * 주차 상태 카드 컴포넌트
 */
@Composable
fun ParkingStatusCard(
    isActive: Boolean,
    session: ParkingSession?,
    duration: String,
    feeResult: FeeResult,
    vehicleDisplay: String? = null,
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
                
                if (!vehicleDisplay.isNullOrBlank()) {
                    Text(
                        text = "차량: $vehicleDisplay",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Text(
                    text = "경과 시간: $duration",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "요금: ${String.format("%.0f", feeResult.discounted)}원",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                
                if (feeResult.hasDiscount) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "할인 전: ${String.format("%.0f", feeResult.original)}원",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondary,
                        textDecoration = TextDecoration.LineThrough
                    )
                    
                    Text(
                        text = "50% 할인 적용",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
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

/**
 * 기존 호환성을 위한 오버로드
 */
@Composable
fun ParkingStatusCard(
    isActive: Boolean,
    session: ParkingSession?,
    duration: String,
    fee: Double,
    modifier: Modifier = Modifier
) {
    val feeResult = FeeResult(original = fee, discounted = fee)
    ParkingStatusCard(
        isActive = isActive,
        session = session,
        duration = duration,
        feeResult = feeResult,
        modifier = modifier
    )
}

private fun formatTime(timestamp: Long): String {
    return TimeUtils.formatTime(timestamp)
}
