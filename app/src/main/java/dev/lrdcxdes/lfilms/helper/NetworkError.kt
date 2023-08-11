package dev.lrdcxdes.lfilms.helper

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.lrdcxdes.lfilms.R

@Composable
fun NetworkError(onRetry: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(LocalContext.current.getString(R.string.network_error_title))
        },
        text = {
            Text(LocalContext.current.getString(R.string.network_error_message))
        },
        confirmButton = {
            Button(
                onClick = onRetry
            ) {
                Text(LocalContext.current.getString(R.string.retry))
            }
        }
    )
}