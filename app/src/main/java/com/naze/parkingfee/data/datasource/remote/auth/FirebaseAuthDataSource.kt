package com.naze.parkingfee.data.datasource.remote.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FirebaseAuth 기반 인증 데이터 소스
 *
 * - 원격 호출 직전에 "로그인 상태 보장"을 위해 사용
 * - 동시 호출 시 중복 로그인 시도를 방지하기 위해 Mutex 사용
 */
@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {
    private val signInMutex = Mutex()

    fun getCurrentUid(): String? = auth.currentUser?.uid

    suspend fun ensureSignedInAnonymously(): Result<Unit> {
        // 빠른 경로: 이미 로그인 되어 있으면 OK
        if (auth.currentUser != null) return Result.success(Unit)

        return signInMutex.withLock {
            // 락 대기 중 다른 코루틴이 로그인했을 수도 있으니 재확인
            if (auth.currentUser != null) return@withLock Result.success(Unit)

            runCatching {
                auth.signInAnonymously().await()
            }.map { Unit }
        }
    }
}

