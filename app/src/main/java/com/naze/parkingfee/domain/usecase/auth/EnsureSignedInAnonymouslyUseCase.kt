package com.naze.parkingfee.domain.usecase.auth

import com.naze.parkingfee.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * 원격(Firestore/Storage 등) 작업 수행 전, 익명 로그인 상태를 보장하는 UseCase
 */
class EnsureSignedInAnonymouslyUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun execute(): Result<Unit> {
        return authRepository.ensureSignedInAnonymously()
    }
}

