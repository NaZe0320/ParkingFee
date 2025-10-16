package com.naze.parkingfee.domain.model

/**
 * 사용자 계정 도메인 모델
 * Block Store와 Firestore에 저장되는 사용자 정보를 나타냅니다.
 */
data class User(
    val id: String,
    val createdAt: Long,
    val lastLoginAt: Long
)
