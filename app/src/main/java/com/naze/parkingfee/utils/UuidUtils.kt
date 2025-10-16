package com.naze.parkingfee.utils

import java.util.UUID

object UuidUtils {
    /**
     * 범용 UUID 생성 메서드
     * 계정 토큰, 주차 세션, 주차 구역, 차량 등 모든 엔티티 ID 생성에 사용
     */
    fun generateUUID(): String {
        // 무작위로 생성된 Type 4 UUID를 가져옵니다.
        val uuid: UUID = UUID.randomUUID()

        // UUID 객체를 하이픈(-)이 포함된 표준 문자열 형식으로 변환하여 반환합니다.
        // 예: "a1b2c3d4-e5f6-7890-1234-567890abcdef"
        return uuid.toString()
    }
}