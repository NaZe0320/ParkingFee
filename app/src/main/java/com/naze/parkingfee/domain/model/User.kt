package com.naze.parkingfee.domain.model

import com.google.firebase.firestore.PropertyName

/**
 * 사용자 계정 도메인 모델
 * Block Store와 Firestore에 저장되는 사용자 정보를 나타냅니다.
 */
data class User(
    @PropertyName("id")
    val id: String = "",
    @PropertyName("createdAt")
    val createdAt: Long = 0L,
    @PropertyName("lastLoginAt")
    val lastLoginAt: Long = 0L
) {
    /**
     * Firestore 직렬화를 위한 no-argument constructor
     */
    constructor() : this("", 0L, 0L)
}
