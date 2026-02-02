package com.naze.parkingfee.presentation.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

@Composable
fun AppUpdateDialog(
    visible: Boolean,
    isForce: Boolean,
    message: String,
    onUpdate: () -> Unit,
    onLater: () -> Unit = {}
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = {
            if (!isForce) onLater()
        },
        title = { Text(text = "업데이트") },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onUpdate) {
                Text(text = "업데이트")
            }
        },
        dismissButton = {
            if (!isForce) {
                TextButton(onClick = onLater) {
                    Text(text = "나중에")
                }
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !isForce,
            dismissOnClickOutside = !isForce
        )
    )
}

