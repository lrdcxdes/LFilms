package dev.lrdcxdes.lfilms.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import dev.lrdcxdes.lfilms.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL


@Composable
fun AutoUpdate() {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var actualVersion by remember { mutableStateOf("") }
    val currentVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName

    // Check for updates on initialization
    LaunchedEffect(Unit) {
        checkForUpdates(currentVersion) { version ->
            actualVersion = version
            if (currentVersion != version) {
                showDialog = true
            }
        }
    }

    val resources = context.resources

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = resources.getString(R.string.update_available)) },
            text = {
                Text(
                    text = resources.getString(
                        R.string.update_available_text,
                        actualVersion
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        downloadAndInstallApk(context)
                        showDialog = false
                    }
                ) {
                    Text(text = resources.getString(R.string.update))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(text = resources.getString(R.string.dismiss))
                }
            }
        )
    }
}

@SuppressLint("Range")
private fun downloadAndInstallApk(context: Context) {
    val url = "https://github.com/lrdcxdes/hdrezka-mirror/releases/latest/download/app-release.apk"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

private suspend fun checkForUpdates(
    currentVersion: String,
    onUpdateAvailable: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val url = URL("https://raw.githubusercontent.com/lrdcxdes/hdrezka-mirror/main/version")
            val connection = url.openConnection()
            val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
            val actualVersion = reader.readLine()?.trim() ?: ""
            reader.close()

            withContext(Dispatchers.Main) {
                if (actualVersion.isNotEmpty() && actualVersion != currentVersion) {
                    onUpdateAvailable(actualVersion)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}