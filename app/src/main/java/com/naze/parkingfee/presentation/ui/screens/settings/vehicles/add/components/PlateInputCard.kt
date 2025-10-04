package com.naze.parkingfee.presentation.ui.screens.settings.vehicles.add.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * 번호판 입력 컴포넌트
 */
@Composable
fun PlateInputCard(
    plateNumber: String,
    onPlateNumberChange: (String) -> Unit,
    plateNumberError: String? = null
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
                text = "번호판 입력",
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedTextField(
                value = plateNumber,
                onValueChange = { value ->
                    // 번호판 형식 자동 포맷팅 (예: 12가3456)
                    val formattedValue = formatPlateNumber(value)
                    onPlateNumberChange(formattedValue)
                },
                label = { Text("번호판 (선택사항)") },
                placeholder = { Text("예: 12가3456") },
                modifier = Modifier.fillMaxWidth(),
                isError = plateNumberError != null,
                supportingText = plateNumberError?.let { { Text(it) } }
            )
            
            Text(
                text = "번호판은 선택사항입니다. 미입력 시 차량 이름으로 구분됩니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 번호판 형식 자동 포맷팅
 */
private fun formatPlateNumber(input: String): String {
    // 숫자와 한글만 허용
    val filtered = input.filter { it.isDigit() || it.isKorean() }
    
    // 최대 7자리로 제한 (12가3456)
    return if (filtered.length <= 7) filtered else filtered.take(7)
}

/**
 * 한글 문자인지 확인
 */
private fun Char.isKorean(): Boolean {
    return this in '\uAC00'..'\uD7AF' || // 한글 완성형
           this in '\u1100'..'\u11FF' || // 한글 자모
           this in '\u3130'..'\u318F'    // 한글 호환 자모
}
