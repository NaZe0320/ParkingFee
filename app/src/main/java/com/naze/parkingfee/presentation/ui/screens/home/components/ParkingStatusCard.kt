package com.naze.parkingfee.presentation.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.naze.parkingfee.domain.model.ParkingSession
import com.naze.parkingfee.domain.model.ParkingZone
import com.naze.parkingfee.utils.TimeUtils
import com.naze.parkingfee.utils.FeeResult

/**
 * 주차 상태 카드 컴포넌트
 * React 디자인의 큰 타이머와 Indigo 배경을 구현합니다.
 */
@Composable
fun ParkingStatusCard(
    isActive: Boolean,
    session: ParkingSession?,
    duration: String,
    feeResult: FeeResult,
    vehicleDisplay: String? = null,
    zoneName: String? = null,
    isExpanded: Boolean = true,
    onToggleExpand: () -> Unit = {},
    selectedZone: ParkingZone? = null,
    onStartParking: (String) -> Unit = {},
    onStopParking: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                top = 12.dp,
                bottom = if (!isActive && !isExpanded) 4.dp else 16.dp,
                start = 24.dp,
                end = 24.dp
            )
        ) {
            // 상단 상태 표시
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 상태 점
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isActive) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                    )
                    
                    Text(
                        text = if (isActive) "주차 중" else "대기",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                // 접힌 상태일 때 시작/정지 버튼과 확장 버튼
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 접힌 상태일 때만 작은 시작/정지 버튼 표시
                    if (!isExpanded) {
                        FilledTonalButton(
                            onClick = {
                                if (!isActive) {
                                    selectedZone?.let { zone ->
                                        onStartParking(zone.id)
                                    }
                                } else {
                                    session?.let { s ->
                                        onStopParking(s.id)
                                    }
                                }
                            },
                                enabled = if (!isActive) selectedZone != null else session != null,
                            modifier = Modifier.width(56.dp).height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isActive) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isActive) "종료" else "시작",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isActive) {
                                    MaterialTheme.colorScheme.onError
                                } else {
                                    MaterialTheme.colorScheme.onPrimary
                                }
                            )
                        }
                    }
                    
                    // 접기/펼치기 버튼
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "접기" else "펼치기",
                            modifier = Modifier.size(20.dp),
                            tint = if (isActive) {
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 큰 타이머 표시 (펼쳤을 때만)
            if (isExpanded) {
                Text(
                    text = if (isActive) duration else "00:00",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            // 접힌 상태일 때는 시간과 비용을 가로로 나란히 배치
            if (!isExpanded && isActive && session != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = duration,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "|",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${String.format("%.0f", feeResult.discounted).replace(",", ",")}원",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            // 펼쳤을 때만 상세 정보 표시
            if (isExpanded) {
                // 주차 중일 때 추가 정보 표시
                if (isActive && session != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 구분선
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                if (isActive) {
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                }
                            )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 현재 요금 표시
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "현재 요금",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                        )
                    Text(
                        text = "${String.format("%.0f", feeResult.discounted).replace(",", ",")}원",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 시작 시간과 위치 정보
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "시작",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                            )
                            Text(
                                text = formatTime(session.startTime),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                        
                        // 구분선
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                        )
                        
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "위치",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                            )
                            Text(
                                text = zoneName ?: session.zoneId,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                    }
                    
                    // 차량 정보가 있으면 표시
                    if (!vehicleDisplay.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "차량: $vehicleDisplay",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                    
                    // 할인 정보가 있으면 표시
                    if (feeResult.hasDiscount) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "경차 할인 적용",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "${String.format("%.0f", feeResult.original)}원",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                textDecoration = TextDecoration.LineThrough
                            )
                            Text(
                                text = "→ -50%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "주차 구역을 선택하고 시작하세요",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 제어 버튼들
                Spacer(modifier = Modifier.height(20.dp))
                ParkingControlButtons(
                    isParkingActive = isActive,
                    selectedZone = selectedZone,
                    activeSession = session,
                    onStartParking = onStartParking,
                    onStopParking = onStopParking
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
