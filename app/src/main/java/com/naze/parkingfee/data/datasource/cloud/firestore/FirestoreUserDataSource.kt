package com.naze.parkingfee.data.datasource.cloud.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.naze.parkingfee.domain.model.User
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Firebase Firestore를 사용한 사용자 데이터 소스
 * 사용자 계정 정보를 클라우드에 저장하고 조회합니다.
 */
@Singleton
class FirestoreUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "FirestoreUserDataSource"
        private const val USERS_COLLECTION = "users"
    }

    /**
     * Firestore에서 사용자 정보를 조회합니다.
     * 
     * @param userId 조회할 사용자 ID
     * @return 사용자 정보 (User?) - 없으면 null
     * @throws Exception Firestore 작업 실패 시
     */
    suspend fun getUserById(userId: String): User? = suspendCancellableCoroutine { continuation ->
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    Log.d(TAG, "사용자 조회 성공: $userId")
                    continuation.resume(user)
                } else {
                    Log.d(TAG, "사용자를 찾을 수 없음: $userId")
                    continuation.resume(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "사용자 조회 실패: ${exception.message}", exception)
                continuation.resumeWithException(exception)
            }
    }

    /**
     * Firestore에 새로운 사용자를 생성합니다.
     * 
     * @param user 생성할 사용자 정보
     * @throws Exception Firestore 작업 실패 시
     */
    suspend fun createUser(user: User): Unit = suspendCancellableCoroutine { continuation ->
        firestore.collection(USERS_COLLECTION)
            .document(user.id)
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "사용자 생성 성공: ${user.id}")
                continuation.resume(Unit)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "사용자 생성 실패: ${exception.message}", exception)
                continuation.resumeWithException(exception)
            }
    }

    /**
     * 사용자의 마지막 로그인 시간을 업데이트합니다.
     * 
     * @param userId 업데이트할 사용자 ID
     * @param lastLoginAt 새로운 마지막 로그인 시간
     * @throws Exception Firestore 작업 실패 시
     */
    suspend fun updateLastLogin(userId: String, lastLoginAt: Long): Unit = suspendCancellableCoroutine { continuation ->
        val updateData = mapOf("lastLoginAt" to lastLoginAt)
        
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .update(updateData)
            .addOnSuccessListener {
                Log.d(TAG, "마지막 로그인 시간 업데이트 성공: $userId")
                continuation.resume(Unit)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "마지막 로그인 시간 업데이트 실패: ${exception.message}", exception)
                continuation.resumeWithException(exception)
            }
    }

    /**
     * Firestore에서 사용자를 삭제합니다.
     * 
     * @param userId 삭제할 사용자 ID
     * @throws Exception Firestore 작업 실패 시
     */
    suspend fun deleteUser(userId: String): Unit = suspendCancellableCoroutine { continuation ->
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "사용자 삭제 성공: $userId")
                continuation.resume(Unit)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "사용자 삭제 실패: ${exception.message}", exception)
                continuation.resumeWithException(exception)
            }
    }
}
