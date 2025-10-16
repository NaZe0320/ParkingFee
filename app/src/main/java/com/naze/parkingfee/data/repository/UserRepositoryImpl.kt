package com.naze.parkingfee.data.repository

import android.util.Log
import com.naze.parkingfee.data.datasource.cloud.firestore.FirestoreUserDataSource
import com.naze.parkingfee.data.datasource.local.blockstore.BlockStoreDataSource
import com.naze.parkingfee.domain.model.User
import com.naze.parkingfee.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 사용자 계정 리포지토리 구현체
 * Block Store와 Firestore를 통합하여 사용자 계정을 관리합니다.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val blockStoreDataSource: BlockStoreDataSource,
    private val firestoreUserDataSource: FirestoreUserDataSource
) : UserRepository {

    companion object {
        private const val TAG = "UserRepositoryImpl"
    }

    override suspend fun initializeUser(): Result<Unit> {
        return try {
            Log.d(TAG, "사용자 계정 초기화 시작")
            
            // Block Store에서 사용자 ID 조회 또는 생성
            val userId = blockStoreDataSource.getOrCreateUserId()
            Log.d(TAG, "사용자 ID 확인/생성 완료: $userId")
            
            // Firestore에서 사용자 정보 조회
            val existingUser = firestoreUserDataSource.getUserById(userId)
            
            if (existingUser != null) {
                // 기존 사용자 - 마지막 로그인 시간 업데이트
                Log.d(TAG, "기존 사용자 발견, 마지막 로그인 시간 업데이트")
                val currentTime = System.currentTimeMillis()
                firestoreUserDataSource.updateLastLogin(userId, currentTime)
            } else {
                // 새 사용자 - Firestore에 생성
                Log.d(TAG, "새 사용자 생성")
                val currentTime = System.currentTimeMillis()
                val newUser = User(
                    id = userId,
                    createdAt = currentTime,
                    lastLoginAt = currentTime
                )
                firestoreUserDataSource.createUser(newUser)
            }
            
            Log.d(TAG, "사용자 계정 초기화 완료")
            Result.success(Unit)
            
        } catch (exception: Exception) {
            Log.e(TAG, "사용자 계정 초기화 실패: ${exception.message}", exception)
            Result.failure(exception)
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val userId = blockStoreDataSource.getOrCreateUserId()
            firestoreUserDataSource.getUserById(userId)
        } catch (exception: Exception) {
            Log.e(TAG, "현재 사용자 조회 실패: ${exception.message}", exception)
            null
        }
    }

    override suspend fun deleteUser(): Result<Unit> {
        return try {
            Log.d(TAG, "사용자 계정 삭제 시작")
            
            // 현재 사용자 ID 조회
            val userId = blockStoreDataSource.getOrCreateUserId()
            Log.d(TAG, "삭제할 사용자 ID: $userId")
            
            // Firestore에서 사용자 삭제
            firestoreUserDataSource.deleteUser(userId)
            
            // Block Store에서 사용자 ID 삭제
            blockStoreDataSource.deleteUserId()
            
            Log.d(TAG, "사용자 계정 삭제 완료")
            Result.success(Unit)
            
        } catch (exception: Exception) {
            Log.e(TAG, "사용자 계정 삭제 실패: ${exception.message}", exception)
            Result.failure(exception)
        }
    }
}
