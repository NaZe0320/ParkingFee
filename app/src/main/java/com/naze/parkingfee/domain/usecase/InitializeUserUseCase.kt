package com.naze.parkingfee.domain.usecase

import android.util.Log
import com.naze.parkingfee.domain.repository.UserRepository
import javax.inject.Inject

/**
 * 사용자 계정 초기화 UseCase
 * 앱 시작 시 사용자 계정을 초기화하고 Block Store와 Firestore를 동기화합니다.
 */
class InitializeUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    companion object {
        private const val TAG = "InitializeUserUseCase"
    }

    /**
     * 사용자 계정을 초기화합니다.
     * 
     * @return 초기화 결과 (성공/실패)
     */
    suspend fun execute(): Result<Unit> {
        return try {
            Log.d(TAG, "사용자 계정 초기화 UseCase 실행")
            val result = userRepository.initializeUser()
            
            if (result.isSuccess) {
                Log.d(TAG, "사용자 계정 초기화 성공")
            } else {
                Log.e(TAG, "사용자 계정 초기화 실패: ${result.exceptionOrNull()?.message}")
            }
            
            result
        } catch (exception: Exception) {
            Log.e(TAG, "사용자 계정 초기화 중 예외 발생: ${exception.message}", exception)
            Result.failure(exception)
        }
    }
}
