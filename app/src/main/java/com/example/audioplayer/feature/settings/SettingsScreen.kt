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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.BuildConfig
import com.example.audioplayer.core.settings.AppThemeColor
import com.example.audioplayer.core.settings.DarkModeSetting
import com.example.audioplayer.ui.sketch.SketchBaseScreen
import com.example.audioplayer.ui.sketch.SketchColorTheme
import com.example.audioplayer.ui.sketch.SketchPill
import com.example.audioplayer.ui.sketch.SketchSecondaryButton
import com.example.audioplayer.ui.sketch.SketchSectionTitle
import com.example.audioplayer.ui.sketch.SketchSettingRow
import com.example.audioplayer.ui.sketch.SketchSpacing
import com.example.audioplayer.ui.sketch.SketchSwitchRow
import com.example.audioplayer.ui.sketch.SketchThemeSwatchGrid
import com.example.audioplayer.ui.sketch.SketchThemeUiModel
import com.example.audioplayer.ui.sketch.SketchToolbar
import com.example.audioplayer.ui.sketch.SketchTopBar
import kotlinx.coroutines.launch

/**
 * 设置页。主题、缓存和权限逻辑保持原实现，只替换为规范化的设置卡片。
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val backgroundPlayback by viewModel.backgroundPlaybackEnabled.collectAsStateWithLifecycle()
    val cacheUsage by viewModel.cacheUsage.collectAsStateWithLifecycle()
    val darkModeSetting by viewModel.darkModeSetting.collectAsStateWithLifecycle()
    val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
    val downloadNetworkOnPlaylistAdd by
        viewModel.downloadNetworkOnPlaylistAdd.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val exportBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                    writer.write(viewModel.createBackup())
                } ?: error("无法创建备份文件")
            }.onSuccess {
                viewModel.showMessage("备份已导出")
            }.onFailure {
                viewModel.showMessage(it.message ?: "备份导出失败")
            }
        }
    }
    val importBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                    reader.readText()
                } ?: error("无法读取备份文件")
            }.onSuccess { viewModel.restoreBackup(it) }
                .onFailure { viewModel.showMessage(it.message ?: "备份读取失败") }
        }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    SketchBaseScreen {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                SketchTopBar(title = "设置")
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = SketchSpacing.Md,
                        bottom = SketchSpacing.Xl,
                    ),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                        SketchSpacing.Sm,
                    ),
                ) {
                    item {
                        SketchSwitchRow(
                            title = "后台播放",
                            subtitle = "退出页面后继续播放音乐",
                            checked = backgroundPlayback,
                            onCheckedChange = viewModel::setBackgroundPlaybackEnabled,
                        )
                    }
                    item {
                        SketchSwitchRow(
                            title = "自动深色模式",
                            subtitle = "开启后跟随手机系统",
                            checked = darkModeSetting == DarkModeSetting.SYSTEM,
                            onCheckedChange = { enabled ->
                                viewModel.setDarkModeSetting(
                                    if (enabled) DarkModeSetting.SYSTEM else DarkModeSetting.LIGHT,
                                )
                            },
                        )
                    }
                    item {
                        SketchToolbar {
                            SketchPill(
                                label = "浅色",
                                selected = darkModeSetting == DarkModeSetting.LIGHT,
                                onClick = {
                                    viewModel.setDarkModeSetting(DarkModeSetting.LIGHT)
                                },
                            )
                            SketchPill(
                                label = "深色",
                                selected = darkModeSetting == DarkModeSetting.DARK,
                                onClick = {
                                    viewModel.setDarkModeSetting(DarkModeSetting.DARK)
                                },
                            )
                        }
                    }
                    item {
                        SketchSectionTitle(
                            text = "颜色主题",
                            modifier = Modifier.padding(horizontal = SketchSpacing.Page),
                        )
                        SketchThemeSwatchGrid(
                            themes = AppThemeColor.entries.map(AppThemeColor::toSketchThemeUiModel),
                            selectedIndex = AppThemeColor.entries.indexOf(themeColor),
                            onSelected = { index ->
                                viewModel.setThemeColor(AppThemeColor.entries[index])
                            },
                        )
                    }
                    item {
                        SketchSettingRow(
                            title = "缓存占用",
                            subtitle = "总计 ${formatBytes(cacheUsage.totalBytes)}，网络 ${
                                formatBytes(cacheUsage.networkBytes)
                            }",
                            trailingText = "清除缓存",
                            onClick = viewModel::clearCache,
                        )
                    }
                    item {
                        SketchSwitchRow(
                            title = "网络歌曲默认下载",
                            subtitle = "添加到播放列表时默认下载到本地",
                            checked = downloadNetworkOnPlaylistAdd,
                            onCheckedChange = viewModel::setDownloadNetworkOnPlaylistAdd,
                        )
                    }
                    item {
                        SketchToolbar {
                            SketchSecondaryButton(
                                label = "导出备份",
                                onClick = { exportBackup.launch("audio-player-backup.json") },
                                modifier = Modifier.weight(1f),
                            )
                            SketchSecondaryButton(
                                label = "恢复备份",
                                onClick = {
                                    importBackup.launch(arrayOf("application/json", "text/plain"))
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
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
                        SketchSettingRow(
                            title = "本地音乐权限",
                            subtitle = if (mediaGranted) "已开启" else "未开启",
                            trailingText = if (mediaGranted) null else "去设置",
                            onClick = { context.openAppSettings() },
                        )
                    }
                    item {
                        val notificationGranted =
                            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS,
                                ) == PackageManager.PERMISSION_GRANTED
                        SketchSettingRow(
                            title = "通知权限",
                            subtitle = if (notificationGranted) "已开启" else "未开启",
                            trailingText = if (notificationGranted) null else "去设置",
                            onClick = { context.openAppSettings() },
                        )
                    }
                    item {
                        val alarmGranted =
                            Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                                context.getSystemService(AlarmManager::class.java)
                                    .canScheduleExactAlarms()
                        SketchSettingRow(
                            title = "精确闹钟权限",
                            subtitle = if (alarmGranted) "已开启" else "未开启",
                            trailingText = if (alarmGranted) null else "去设置",
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                        ).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        },
                                    )
                                }
                            },
                        )
                    }
                    item {
                        SketchSecondaryButton(
                            label = "重新扫描本地音乐",
                            onClick = viewModel::rescanLocalMusic,
                            modifier = Modifier.padding(horizontal = SketchSpacing.Page),
                        )
                    }
                    item {
                        SketchSettingRow(
                            title = "音频播放器",
                            subtitle = "版本 ${BuildConfig.VERSION_NAME}",
                            trailingText = "",
                        )
                    }
                }
            }
        }
    }
}

private fun AppThemeColor.toSketchThemeUiModel(): SketchThemeUiModel {
    val sketchTheme = when (this) {
        AppThemeColor.SKY_BLUE -> SketchColorTheme.SKY_BLUE
        AppThemeColor.FOREST_GREEN -> SketchColorTheme.FOREST_GREEN
        AppThemeColor.VIOLET -> SketchColorTheme.VIOLET
        AppThemeColor.SUNSET_ORANGE -> SketchColorTheme.SUNSET_ORANGE
        AppThemeColor.SAKURA_PINK -> SketchColorTheme.SAKURA_PINK
        AppThemeColor.TEAL -> SketchColorTheme.TEAL
    }
    return SketchThemeUiModel(
        name = sketchTheme.displayName,
        startColor = sketchTheme.primary,
        endColor = sketchTheme.primaryDark,
    )
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
