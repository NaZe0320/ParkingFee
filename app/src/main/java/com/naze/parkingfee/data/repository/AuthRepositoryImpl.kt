package com.naze.parkingfee.data.repository

import com.naze.parkingfee.data.datasource.remote.auth.FirebaseAuthDataSource
import com.naze.parkingfee.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AuthRepository 구현체
 *
 * Domain/Presentation 레이어가 Firebase 타입에 직접 의존하지 않도록,
 * FirebaseAuth 관련 호출은 Data(remote)에서만 수행한다.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource
) : AuthRepository {

    override suspend fun ensureSignedInAnonymously(): Result<Unit> {
        return firebaseAuthDataSource.ensureSignedInAnonymously()
    }

    override fun getCurrentUid(): String? {
        return firebaseAuthDataSource.getCurrentUid()
    }
}

