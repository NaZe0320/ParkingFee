package com.naze.parkingfee.data.datasource.remote.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.naze.parkingfee.data.datasource.remote.firestore.dto.ParkingHistoryDoc
import com.naze.parkingfee.domain.model.ParkingHistory
import com.naze.parkingfee.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ParkingHistory Firestore 구현체 (로깅/기록용)
 *
 * - 읽기/구독은 하지 않는다.
 * - users/{uid}/parkingHistories/{historyId} 에 upsert/delete 한다.
 */
@Singleton
class ParkingHistoryFirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : ParkingHistoryRemoteDataSource {

    override suspend fun upsertParkingHistory(history: ParkingHistory): Result<Unit> {
        val authResult = authRepository.ensureSignedInAnonymously()
        if (authResult.isFailure) return Result.failure(authResult.exceptionOrNull()!!)

        val uid = authRepository.getCurrentUid()
            ?: return Result.failure(IllegalStateException("uid is null after ensureSignedInAnonymously"))

        return runCatching {
            val doc = ParkingHistoryDoc.fromDomain(history).toMap()
            firestore.collection("users")
                .document(uid)
                .collection("parkingHistories")
                .document(history.id)
                .set(doc)
                .await()
        }.map { Unit }
    }

    override suspend fun deleteParkingHistory(historyId: String): Result<Unit> {
        val authResult = authRepository.ensureSignedInAnonymously()
        if (authResult.isFailure) return Result.failure(authResult.exceptionOrNull()!!)

        val uid = authRepository.getCurrentUid()
            ?: return Result.failure(IllegalStateException("uid is null after ensureSignedInAnonymously"))

        return runCatching {
            firestore.collection("users")
                .document(uid)
                .collection("parkingHistories")
                .document(historyId)
                .delete()
                .await()
        }.map { Unit }
    }

    override suspend fun fetchParkingHistories(): Result<List<ParkingHistory>> {
        return Result.failure(NotImplementedError("Firestore read is out of scope (logging-only)"))
    }
}

