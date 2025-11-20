package com.naze.parkingfee.infrastructure.notification

import android.content.Context
import android.widget.Toast

/**
 * Toast 메시지 표시를 담당하는 공통 관리자
 */
object ToastManager {

    /**
     * 짧은 길이의 Toast 메시지를 표시합니다.
     *
     * @param context Toast 표시를 위한 Context
     * @param message 화면에 표시할 메시지
     */
    fun show(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

