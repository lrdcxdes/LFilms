package dev.lrdcxdes.lfilms.helper

import android.content.Context
import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import dev.lrdcxdes.lfilms.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.File


@Composable
fun AutoUpdate(client: OkHttpClient) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var actualVersion by remember { mutableStateOf("") }
    val currentVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName

    val scope = rememberCoroutineScope()

    // Check for updates on initialization
    LaunchedEffect(Unit) {
        checkForUpdates(currentVersion, client) { version ->
            actualVersion = version
            showDialog = true
        }
    }

    val resources = context.resources

    if (showDialog) {
        AlertDialog(onDismissRequest = { showDialog = false },
            title = { Text(text = resources.getString(R.string.update_available)) },
            text = {
                Text(
                    text = resources.getString(
                        R.string.update_available_text, actualVersion
                    )
                )
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        downloadAndInstallApk(context, actualVersion, client)
                    }
                    showDialog = false
                }) {
                    Text(text = resources.getString(R.string.update))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(text = resources.getString(R.string.dismiss))
                }
            })
    }
}

private suspend fun downloadApk(url: String, outputFile: File, client: OkHttpClient) {
    withContext(Dispatchers.IO) {
        val request = okhttp3.Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val inputStream = response.body?.byteStream()
        val outputStream = outputFile.outputStream()

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
    }
}


private fun installApk(context: Context, apkFile: File) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        val apkUri = FileProvider.getUriForFile(
            context, context.applicationContext.packageName + ".provider", apkFile
        )
        setDataAndType(apkUri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    // start the activity
    context.startActivity(intent)
}

private fun getApkFile(context: Context, version: String): File {
    val outputDir = context.cacheDir
    return File(outputDir, "app-release-$version.apk")
}

suspend fun downloadAndInstallApk(
    context: Context, version: String, client: OkHttpClient
) {
    val url = "https://github.com/lrdcxdes/LFilms/releases/latest/download/app-release.apk"
    val apkFile = getApkFile(context, version)

    try {
        if (!apkFile.exists()) {
            downloadApk(url, apkFile, client)
        }
        installApk(context, apkFile)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


suspend fun checkForUpdates(
    currentVersion: String, client: OkHttpClient, onUpdateAvailable: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val request = okhttp3.Request.Builder()
                .url("https://raw.githubusercontent.com/lrdcxdes/LFilms/master/app/build.gradle.kts")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()

            var actualVersion = ""
            if (body != null) {
                val regex = Regex("val vName = \"(.*)\"")
                val matchResult = regex.find(body)
                if (matchResult != null) {
                    actualVersion = matchResult.groupValues[1]
                }
            }

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