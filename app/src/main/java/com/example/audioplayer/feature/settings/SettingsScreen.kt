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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.BuildConfig
import com.example.audioplayer.core.settings.AppThemeColor
import com.example.audioplayer.core.settings.DarkModeSetting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val backgroundPlayback by viewModel.backgroundPlaybackEnabled.collectAsStateWithLifecycle()
    val cacheUsage by viewModel.cacheUsage.collectAsStateWithLifecycle()
    val darkModeSetting by viewModel.darkModeSetting.collectAsStateWithLifecycle()
    val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
            containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
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
                Text("外观", style = MaterialTheme.typography.titleMedium)
                ListItem(
                    headlineContent = { Text("自动深色模式") },
                    supportingContent = { Text("开启后跟随手机系统") },
                    trailingContent = {
                        Switch(
                            checked = darkModeSetting == DarkModeSetting.SYSTEM,
                            onCheckedChange = { enabled ->
                                viewModel.setDarkModeSetting(
                                    if (enabled) DarkModeSetting.SYSTEM else DarkModeSetting.LIGHT,
                                )
                            },
                        )
                    },
                )
                if (darkModeSetting != DarkModeSetting.SYSTEM) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = darkModeSetting == DarkModeSetting.LIGHT,
                            onClick = { viewModel.setDarkModeSetting(DarkModeSetting.LIGHT) },
                            label = { Text("浅色") },
                        )
                        FilterChip(
                            selected = darkModeSetting == DarkModeSetting.DARK,
                            onClick = { viewModel.setDarkModeSetting(DarkModeSetting.DARK) },
                            label = { Text("深色") },
                        )
                    }
                }
                Text("颜色主题", style = MaterialTheme.typography.bodyLarge)
                AppThemeColor.entries.chunked(2).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        row.forEach { color ->
                            ThemeTile(
                                color = color,
                                selected = themeColor == color,
                                onClick = { viewModel.setThemeColor(color) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            item {
                Text("缓存", style = MaterialTheme.typography.titleMedium)
                ListItem(
                    headlineContent = { Text("缓存占用") },
                    supportingContent = {
                        Text("总计 ${formatBytes(cacheUsage.totalBytes)}，网络 ${formatBytes(cacheUsage.networkBytes)}")
                    },
                    trailingContent = {
                        Button(onClick = viewModel::clearCache) { Text("清除缓存") }
                    },
                )
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
                PermissionRow("本地音乐权限", mediaGranted) { context.openAppSettings() }
            }
            item {
                val notificationGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
                PermissionRow("通知权限", notificationGranted) { context.openAppSettings() }
            }
            item {
                val alarmGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                    context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
                PermissionRow("精确闹钟权限", alarmGranted) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:${context.packageName}")
                            },
                        )
                    }
                }
            }
            item {
                OutlinedButton(
                    onClick = viewModel::rescanLocalMusic,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("重新扫描本地音乐") }
            }

            item { HorizontalDivider() }
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
private fun PermissionRow(title: String, granted: Boolean, onOpenSettings: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title)
            Text(
                if (granted) "已开启" else "未开启",
                style = MaterialTheme.typography.bodySmall,
                color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
        }
        if (!granted) Button(onClick = onOpenSettings) { Text("去开启") }
    }
}

private fun themeLabel(color: AppThemeColor): String = when (color) {
    AppThemeColor.SKY_BLUE -> "天空蓝"
    AppThemeColor.FOREST_GREEN -> "森林绿"
    AppThemeColor.VIOLET -> "紫罗兰"
    AppThemeColor.SUNSET_ORANGE -> "日落橙"
    AppThemeColor.SAKURA_PINK -> "樱花粉"
    AppThemeColor.TEAL -> "青绿色"
}

@Composable
private fun ThemeTile(
    color: AppThemeColor,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = when (color) {
        AppThemeColor.SKY_BLUE -> listOf(Color(0xFF277CC1), Color(0xFF42B4AC))
        AppThemeColor.FOREST_GREEN -> listOf(Color(0xFF3E8D58), Color(0xFF9EC76B))
        AppThemeColor.VIOLET -> listOf(Color(0xFF7E4FBB), Color(0xFFB17BD3))
        AppThemeColor.SUNSET_ORANGE -> listOf(Color(0xFFD4762B), Color(0xFFF1B65B))
        AppThemeColor.SAKURA_PINK -> listOf(Color(0xFFC94F7C), Color(0xFFF0A6BD))
        AppThemeColor.TEAL -> listOf(Color(0xFF0D9488), Color(0xFF5BBFAF))
    }
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(colors))
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = androidx.compose.ui.Alignment.BottomStart,
    ) {
        Text(
            text = if (selected) "✓ ${themeLabel(color)}" else themeLabel(color),
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

private fun formatBytes(bytes: Long): String {
    val mb = bytes / 1024.0 / 1024.0
    return if (mb >= 1.0) "%.1f MB".format(mb) else "%.0f KB".format(bytes / 1024.0)
}

private fun android.content.Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        },
    )
}
