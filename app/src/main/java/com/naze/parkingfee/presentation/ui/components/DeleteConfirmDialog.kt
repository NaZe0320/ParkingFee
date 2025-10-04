package com.naze.parkingfee.presentation.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

/**
 * 재사용 가능한 삭제 확인 다이얼로그 컴포넌트
 */
@Composable
fun DeleteConfirmDialog(
    visible: Boolean,
    title: String = "구역 삭제",
    message: String,
    confirmText: String = "삭제",
    dismissText: String = "취소",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(dismissText)
                }
            }
        )
    }
}
