package com.naze.parkingfee.data.datasource.local.blockstore

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.blockstore.Blockstore
import com.google.android.gms.auth.blockstore.BlockstoreClient
import com.google.android.gms.auth.blockstore.RetrieveBytesRequest
import com.google.android.gms.auth.blockstore.RetrieveBytesResponse
import com.google.android.gms.auth.blockstore.StoreBytesData
import com.naze.parkingfee.utils.UuidUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Block Store API를 사용한 사용자 ID 저장/조회 데이터 소스
 * 디바이스에 고유한 사용자 ID를 안전하게 저장하고 조회합니다.
 */
@Singleton
class BlockStoreDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "BlockStoreDataSource"
        private const val USER_ID_KEY = "user_id_key"
    }

    private val blockstoreClient: BlockstoreClient by lazy {
        Blockstore.getClient(context)
    }

    /**
     * Block Store에서 사용자 ID를 조회합니다.
     * ID가 없으면 새로 생성하여 저장한 후 반환합니다.
     * 
     * @return 사용자 ID (String)
     * @throws Exception Block Store 작업 실패 시
     */
    suspend fun getOrCreateUserId(): String = suspendCancellableCoroutine { continuation ->
        val retrieveRequest = RetrieveBytesRequest.Builder()
            .setKeys(listOf(USER_ID_KEY))
            .build()

        blockstoreClient.retrieveBytes(retrieveRequest)
            .addOnSuccessListener { result: RetrieveBytesResponse ->
                val dataMap = result.blockstoreDataMap
                val userIdData = dataMap[USER_ID_KEY]?.bytes

                if (userIdData != null) {
                    // 기존 ID가 있는 경우
                    val userId = String(userIdData, Charsets.UTF_8)
                    Log.d(TAG, "기존 사용자 ID 복원 성공: $userId")
                    continuation.resume(userId)
                } else {
                    // ID가 없는 경우 새로 생성하여 저장
                    Log.d(TAG, "저장된 사용자 ID가 없어 새로 생성합니다.")
                    createAndSaveUserId(continuation)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "사용자 ID 조회 실패: ${exception.message}", exception)
                // 조회 실패 시에도 새로 생성 시도
                createAndSaveUserId(continuation)
            }
    }

    /**
     * 새로운 사용자 ID를 생성하여 Block Store에 저장합니다.
     */
    private fun createAndSaveUserId(continuation: kotlin.coroutines.Continuation<String>) {
        val newUserId = UuidUtils.generateUUID()
        val newUserIdBytes = newUserId.toByteArray(Charsets.UTF_8)

        val storeRequest = StoreBytesData.Builder()
            .setShouldBackupToCloud(true)
            .setBytes(newUserIdBytes)
            .setKey(USER_ID_KEY)
            .build()

        blockstoreClient.storeBytes(storeRequest)
            .addOnSuccessListener { result: Int ->
                Log.d(TAG, "새로운 사용자 ID 저장 성공: $newUserId")
                continuation.resume(newUserId)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "새로운 사용자 ID 저장 실패: ${exception.message}", exception)
                continuation.resumeWithException(exception)
            }
    }

    /**
     * Block Store에서 사용자 ID를 삭제합니다.
     * 
     * @throws Exception Block Store 작업 실패 시
     */
    suspend fun deleteUserId(): Unit = suspendCancellableCoroutine { continuation ->
        val deleteRequest = RetrieveBytesRequest.Builder()
            .setKeys(listOf(USER_ID_KEY))
            .build()

        blockstoreClient.retrieveBytes(deleteRequest)
            .addOnSuccessListener { result: RetrieveBytesResponse ->
                val dataMap = result.blockstoreDataMap
                val userIdData = dataMap[USER_ID_KEY]?.bytes

                if (userIdData != null) {
                    // ID가 있으면 삭제 (실제로는 빈 데이터로 덮어쓰기)
                    val emptyBytes = ByteArray(0)
                    val storeRequest = StoreBytesData.Builder()
                        .setBytes(emptyBytes)
                        .setKey(USER_ID_KEY)
                        .build()

                    blockstoreClient.storeBytes(storeRequest)
                        .addOnSuccessListener {
                            Log.d(TAG, "사용자 ID 삭제 성공")
                            continuation.resume(Unit)
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "사용자 ID 삭제 실패: ${exception.message}", exception)
                            continuation.resumeWithException(exception)
                        }
                } else {
                    // ID가 없으면 이미 삭제된 상태
                    Log.d(TAG, "사용자 ID가 이미 삭제된 상태입니다")
                    continuation.resume(Unit)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "사용자 ID 삭제 확인 실패: ${exception.message}", exception)
                continuation.resumeWithException(exception)
            }
    }
}
