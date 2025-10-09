package com.naze.parkingfee.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape 시스템
 * React 디자인의 rounded 스타일을 Material3 Shapes로 변환
 */
val Shapes = Shapes(
    // rounded-xl (12dp)
    extraSmall = RoundedCornerShape(8.dp),
    
    // rounded-xl (12dp)
    small = RoundedCornerShape(12.dp),
    
    // rounded-2xl (16dp)
    medium = RoundedCornerShape(16.dp),
    
    // rounded-3xl (24dp)
    large = RoundedCornerShape(24.dp),
    
    // 완전한 원형
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * 커스텀 Shape 정의
 */
object CustomShapes {
    // 카드용 둥근 모서리
    val cardShape = RoundedCornerShape(16.dp)
    
    // 큰 카드용 둥근 모서리 (주차 상태 카드 등)
    val largeCardShape = RoundedCornerShape(24.dp)
    
    // 버튼용 둥근 모서리
    val buttonShape = RoundedCornerShape(12.dp)
    
    // 입력 필드용 둥근 모서리
    val textFieldShape = RoundedCornerShape(12.dp)
    
    // 배지용 둥근 모서리
    val badgeShape = RoundedCornerShape(6.dp)
    
    // 토글 스위치용 완전한 원형
    val toggleShape = RoundedCornerShape(50)
}

