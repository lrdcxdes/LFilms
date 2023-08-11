package dev.lrdcxdes.lfilms.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import dev.lrdcxdes.lfilms.R
import dev.lrdcxdes.lfilms.Theme

@Composable
fun SettingsScreen(
    navBar: @Composable () -> Unit,
    currentTheme: Theme,
    currentMirror: String,
    onMirrorChanged: (String) -> Unit,
    onResetToActualMirror: () -> Unit,
    onThemeChanged: (Theme) -> Unit,
    context: Context
) {
    val resources = context.resources

    Scaffold(
        bottomBar = { navBar() },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = paddingValues
            ) {
                item {
                    Text(
                        text = resources.getString(R.string.settings),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                // Add a theme setting item with options
                item {
                    ThemeSettingItem(
                        selectedTheme = currentTheme,
                        onThemeSelected = onThemeChanged
                    )
                }
                // Add a domain mirror setting item with input field
                item {
                    DomainMirrorSettingItem(
                        currentMirror = currentMirror,
                        onMirrorChanged = { newMirror ->
                            onMirrorChanged(newMirror)
                        },
                        onResetToActualMirror = onResetToActualMirror
                    )
                }
                item {
                    // github link
                    SettingItem(
                        icon = ImageVector.vectorResource(R.drawable.ic_github),
                        label = "GitHub",
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse("https://github.com/lrdcxdes/LFilms")
                            context.startActivity(intent)
                        },
                    )
                }
                // Add more settings items as needed
            }
        }
    )
}


@Composable
fun SettingItem(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ThemeSettingItem(
    selectedTheme: Theme,
    onThemeSelected: (Theme) -> Unit
) {
    val themeOptions = listOf(Theme.LIGHT, Theme.DARK)
    var showDialog by remember { mutableStateOf(false) }

    SettingItem(
        icon = if (selectedTheme == Theme.LIGHT) {
            Icons.Default.Edit
        } else {
            Icons.Default.Edit
        },
        label = LocalContext.current.resources.getString(R.string.theme),
        modifier = Modifier.clickable {
            showDialog = true
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text(LocalContext.current.resources.getString(R.string.confirm))
                }
            },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            title = {
                Text(
                    text = LocalContext.current.resources.getString(R.string.select_theme)
                )
            },
            text = {
                // Implement the list of themes in the dialog
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(themeOptions) { theme ->
                        Text(
                            text = theme.name,
                            modifier = Modifier
                                .clickable {
                                    onThemeSelected(theme)
                                    showDialog = false
                                }
                                .padding(16.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text(LocalContext.current.resources.getString(R.string.dismiss))
                }
            }
        )
    }
}

@Composable
fun DomainMirrorSettingItem(
    currentMirror: String,
    onMirrorChanged: (String) -> Unit,
    onResetToActualMirror: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var newMirror by remember { mutableStateOf(currentMirror) }

    SettingItem(
        icon = Icons.Default.Edit,
        label = LocalContext.current.resources.getString(R.string.domain_mirror),
        modifier = Modifier.clickable {
            showDialog = true
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        onMirrorChanged(newMirror)
                        showDialog = false
                    },
                ) {
                    Text(LocalContext.current.resources.getString(R.string.confirm))
                }
            },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            title = {
                Text(
                    text = LocalContext.current.resources.getString(R.string.enter_domain_mirror)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = newMirror,
                        onValueChange = { newMirror = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Show (Reset to actual domain mirror) button

                    Button(
                        onClick = {
                            onResetToActualMirror()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = LocalContext.current.resources.getString(R.string.reset_to_actual_domain_mirror)
                        )
                    }
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text(LocalContext.current.resources.getString(R.string.dismiss))
                }
            }
        )
    }
}
