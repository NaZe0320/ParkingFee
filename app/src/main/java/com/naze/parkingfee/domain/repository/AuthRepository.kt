package com.naze.parkingfee.domain.repository

/**
 * 인증 관련 데이터 접근을 위한 Repository 인터페이스
 *
 * - 원격(Firestore/Storage 등) 호출 직전에 로그인 상태를 보장하기 위한 용도
 * - Domain/Presentation 레이어에서는 Firebase 타입을 직접 알지 않도록 한다.
 */
interface AuthRepository {

    /**
     * 로그인되어 있지 않다면 익명 로그인을 수행하여, 이후 원격 호출이 가능하도록 보장합니다.
     */
    suspend fun ensureSignedInAnonymously(): Result<Unit>

    /**
     * 현재 로그인된 사용자 UID (없으면 null)
     */
    fun getCurrentUid(): String?
}

