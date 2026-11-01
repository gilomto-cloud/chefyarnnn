package com.trynite.chefyarnnn.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trynite.chefyarnnn.ui.viewmodel.AppUpdateState

@Composable
fun AppUpdateDialog(
    state: AppUpdateState,
    onInstall: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    when (state) {
        AppUpdateState.Checking,
        AppUpdateState.UpToDate,
        AppUpdateState.Dismissed -> Unit

        is AppUpdateState.Downloading -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Nada-download ang update") },
            text = {
                Column {
                    Text("Bersyon ${state.versionName} ang mada-download.")
                    Spacer(Modifier.height(16.dp))
                    if (state.progress == null) {
                        CircularProgressIndicator()
                    } else {
                        Text("${state.progress}%")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Mamaya") }
            }
        )

        is AppUpdateState.Ready -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("May bagong bersyon") },
            text = {
                Text(
                    "Na-download na ang bersyon ${state.update.versionName}. " +
                        "Kailangan mong aprubahan ang pag-install sa Android."
                )
            },
            confirmButton = {
                Button(onClick = onInstall) { Text("I-install") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Mamaya") }
            }
        )

        is AppUpdateState.Failed -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Hindi makumpleto ang update") },
            text = { Text(state.message) },
            confirmButton = {
                Button(onClick = onRetry) { Text("Subukan muli") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Isara") }
            }
        )
    }
}
