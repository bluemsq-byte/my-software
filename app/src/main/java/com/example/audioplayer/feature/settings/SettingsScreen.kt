package com.example.audioplayer.feature.settings

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    val cacheUsage by viewModel.cacheUsage.collectAsStateWithLifecycle()
    val bluetoothDevices by viewModel.bluetoothDevices.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        viewModel.bluetoothRouteManager.refresh()
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("设置") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                Text("缓存", style = MaterialTheme.typography.titleMedium)
                ListItem(
                    headlineContent = { Text("缓存占用") },
                    supportingContent = {
                        Text(
                            "总计 ${formatBytes(cacheUsage.totalBytes)}，网络 ${formatBytes(cacheUsage.networkBytes)}",
                        )
                    },
                    trailingContent = {
                        Button(onClick = viewModel::clearCache) { Text("清除缓存") }
                    },
                )
            }

            item {
                Text("蓝牙输出", style = MaterialTheme.typography.titleMedium)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    Button(
                        onClick = {
                            bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                        },
                    ) {
                        Text("授权并扫描蓝牙设备")
                    }
                } else {
                    OutlinedButton(
                        onClick = { viewModel.bluetoothRouteManager.refresh() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("刷新蓝牙输出设备")
                    }
                }
                if (bluetoothDevices.isEmpty()) {
                    Text("未发现已配对的蓝牙音频设备")
                } else {
                    bluetoothDevices.forEach { device ->
                        ListItem(
                            headlineContent = { Text(device.name) },
                            supportingContent = { Text(if (device.isSelected) "当前输出" else "点击尝试切换") },
                            trailingContent = {
                                Button(
                                    onClick = { viewModel.selectBluetoothDevice(device.id) },
                                    enabled = !device.isSelected,
                                ) { Text("选择") }
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
private fun formatBytes(bytes: Long): String {
    val mb = bytes / 1024.0 / 1024.0
    return if (mb >= 1.0) "%.1f MB".format(mb) else "%.0f KB".format(bytes / 1024.0)
}
