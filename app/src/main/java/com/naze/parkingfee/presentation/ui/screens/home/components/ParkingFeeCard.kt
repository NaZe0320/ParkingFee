package com.naze.parkingfee.presentation.ui.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.naze.parkingfee.utils.FeeResult

/**
 * 주차 요금 정보 카드 컴포넌트
 */
@Composable
fun ParkingFeeCard(
    feeResult: FeeResult,
    duration: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "주차 요금",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "경과 시간",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                    Text(
                        text = duration,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "요금",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                    
                    if (feeResult.hasDiscount) {
                        // 할인 전 금액 (취소선)
                        Text(
                            text = "${String.format("%.0f", feeResult.original)}원",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiary,
                            textDecoration = TextDecoration.LineThrough
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // 할인 후 금액 (강조)
                        Text(
                            text = "${String.format("%.0f", feeResult.discounted)}원",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                        
                        // 할인 정보
                        Text(
                            text = "50% 할인 적용",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    } else {
                        // 할인 없음
                        Text(
                            text = "${String.format("%.0f", feeResult.discounted)}원",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }
        }
    }
}

/**
 * 기존 호환성을 위한 오버로드
 */
@Composable
fun ParkingFeeCard(
    fee: Double,
    duration: String,
    modifier: Modifier = Modifier
) {
    val feeResult = FeeResult(original = fee, discounted = fee)
    ParkingFeeCard(feeResult = feeResult, duration = duration, modifier = modifier)
}
