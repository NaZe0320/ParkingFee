package com.naze.parkingfee.data.datasource.remote.firestore.dto

import com.naze.parkingfee.domain.model.ParkingHistory

/**
 * Firestore 저장용 ParkingHistory 문서 DTO
 *
 * - 앱의 메인은 Room이며, Firestore는 "기록/로깅" 용도로만 저장한다.
 * - Firestore 문서는 users/{uid}/parkingHistories/{historyId}에 저장한다.
 */
data class ParkingHistoryDoc(
    val id: String,
    val zoneId: String,
    val zoneNameSnapshot: String,
    val vehicleId: String?,
    val vehicleNameSnapshot: String?,
    val vehiclePlateSnapshot: String?,
    val startedAt: Long,
    val endedAt: Long,
    val durationMinutes: Int,
    val feePaid: Double,
    val originalFee: Double?,
    val hasDiscount: Boolean,
    val createdAt: Long
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "zoneId" to zoneId,
            "zoneNameSnapshot" to zoneNameSnapshot,
            "startedAt" to startedAt,
            "endedAt" to endedAt,
            "durationMinutes" to durationMinutes,
            "feePaid" to feePaid,
            "hasDiscount" to hasDiscount,
            "createdAt" to createdAt
        )

        vehicleId?.let { map["vehicleId"] = it }
        vehicleNameSnapshot?.let { map["vehicleNameSnapshot"] = it }
        vehiclePlateSnapshot?.let { map["vehiclePlateSnapshot"] = it }
        originalFee?.let { map["originalFee"] = it }

        return map
    }

    companion object {
        fun fromDomain(history: ParkingHistory): ParkingHistoryDoc {
            return ParkingHistoryDoc(
                id = history.id,
                zoneId = history.zoneId,
                zoneNameSnapshot = history.zoneNameSnapshot,
                vehicleId = history.vehicleId,
                vehicleNameSnapshot = history.vehicleNameSnapshot,
                vehiclePlateSnapshot = history.vehiclePlateSnapshot,
                startedAt = history.startedAt,
                endedAt = history.endedAt,
                durationMinutes = history.durationMinutes,
                feePaid = history.feePaid,
                originalFee = history.originalFee,
                hasDiscount = history.hasDiscount,
                createdAt = history.createdAt
            )
        }
    }
}

