package com.naze.parkingfee.utils

import android.util.Log

/**
 * 테스트/디버그용 동기화 로거
 *
 * - 지금 단계에서는 Firestore/Storage 저장을 실제로 하지 않고, "저장 예정" 흐름만 Log로 남긴다.
 * - 추후 실제 원격 동기화가 들어가면 제거/축소될 수 있다.
 */
object SyncLogger {
    private const val TAG = "SyncLogger"

    fun logSyncAttempt(entityType: String, entityId: String?, uid: String?) {
        Log.i(TAG, "sync 예정: type=$entityType id=$entityId uid=$uid")
    }

    fun logSyncSkipped(entityType: String, entityId: String?, reason: String) {
        Log.w(TAG, "sync 스킵: type=$entityType id=$entityId reason=$reason")
    }
}

