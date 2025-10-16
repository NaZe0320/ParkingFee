package com.naze.parkingfee.domain.usecase

import android.util.Log
import com.naze.parkingfee.domain.repository.UserRepository
import javax.inject.Inject

/**
 * 사용자 계정 삭제 UseCase
 * 사용자 탈퇴 시 Block Store와 Firestore에서 모든 사용자 데이터를 삭제합니다.
 */
class DeleteUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    companion object {
        private const val TAG = "DeleteUserUseCase"
    }

    /**
     * 사용자 계정을 삭제합니다.
     * 
     * @return 삭제 결과 (성공/실패)
     */
    suspend fun execute(): Result<Unit> {
        return try {
            Log.d(TAG, "사용자 계정 삭제 UseCase 실행")
            val result = userRepository.deleteUser()
            
            if (result.isSuccess) {
                Log.d(TAG, "사용자 계정 삭제 성공")
            } else {
                Log.e(TAG, "사용자 계정 삭제 실패: ${result.exceptionOrNull()?.message}")
            }
            
            result
        } catch (exception: Exception) {
            Log.e(TAG, "사용자 계정 삭제 중 예외 발생: ${exception.message}", exception)
            Result.failure(exception)
        }
    }
}
