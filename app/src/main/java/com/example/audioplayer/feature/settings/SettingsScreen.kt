package com.example.audioplayer.feature.settings

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val connections by viewModel.connections.collectAsStateWithLifecycle()
    val backgroundPlayback by viewModel.backgroundPlaybackEnabled.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("设置") }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("播放", style = MaterialTheme.typography.titleMedium)
                ListItem(
                    headlineContent = { Text("后台播放") },
                    supportingContent = { Text("退出页面后继续播放音乐") },
                    trailingContent = {
                        Switch(
                            checked = backgroundPlayback,
                            onCheckedChange = viewModel::setBackgroundPlaybackEnabled,
                        )
                    },
                )
            }

            item {
                Text("NAS 连接", style = MaterialTheme.typography.titleMedium)
            }
            if (connections.isEmpty()) {
                item { Text("尚未添加 NAS 连接") }
            } else {
                items(connections, key = { it.id }) { connection ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        ListItem(
                            headlineContent = { Text(connection.name) },
                            supportingContent = {
                                Text("${connection.protocol.name} · ${connection.host}")
                            },
                            leadingContent = { Icon(Icons.Default.Folder, contentDescription = null) },
                            trailingContent = {
                                IconButton(onClick = { viewModel.deleteConnection(connection) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "删除")
                                }
                            },
                        )
                    }
                }
            }

            item {
                Text("权限和系统设置", style = MaterialTheme.typography.titleMedium)
            }
            item {
                val mediaGranted = ContextCompat.checkSelfPermission(
                    context,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_AUDIO
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                ) == PackageManager.PERMISSION_GRANTED
                PermissionRow(
                    title = "本地音乐权限",
                    granted = mediaGranted,
                    onOpenSettings = { context.openAppSettings() },
                )
            }
            item {
                val notificationGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED
                PermissionRow(
                    title = "通知权限",
                    granted = notificationGranted,
                    onOpenSettings = { context.openAppSettings() },
                )
            }
            item {
                val alarmGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                    context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
                PermissionRow(
                    title = "精确闹钟权限",
                    granted = alarmGranted,
                    onOpenSettings = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            context.startActivity(
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                },
                            )
                        }
                    },
                )
            }
            item {
                OutlinedButton(
                    onClick = viewModel::rescanLocalMusic,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("重新扫描本地音乐")
                }
            }

            item {
                HorizontalDivider()
            }
            item {
                ListItem(
                    headlineContent = { Text("音频播放器") },
                    supportingContent = { Text("版本 ${BuildConfig.VERSION_NAME}") },
                )
            }
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    granted: Boolean,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title)
            Text(
                if (granted) "已开启" else "未开启",
                style = MaterialTheme.typography.bodySmall,
                color = if (granted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        }
        if (!granted) {
            Button(onClick = onOpenSettings) { Text("去开启") }
        }
    }
}

private fun android.content.Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        },
    )
}