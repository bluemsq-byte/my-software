package com.example.audioplayer.feature.timer

import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.core.model.TimerAction
import com.example.audioplayer.core.model.TimerSelectionType
import com.example.audioplayer.core.model.TimerSourceType
import com.example.audioplayer.ui.sketch.SketchBaseScreen
import com.example.audioplayer.ui.sketch.SketchEditableField
import com.example.audioplayer.ui.sketch.SketchEmptyState
import com.example.audioplayer.ui.sketch.SketchField
import com.example.audioplayer.ui.sketch.SketchGlassCard
import com.example.audioplayer.ui.sketch.SketchIconAction
import com.example.audioplayer.ui.sketch.SketchPill
import com.example.audioplayer.ui.sketch.SketchPrimaryButton
import com.example.audioplayer.ui.sketch.SketchSecondaryButton
import com.example.audioplayer.ui.sketch.SketchSegmentedTabs
import com.example.audioplayer.ui.sketch.SketchSettingRow
import com.example.audioplayer.ui.sketch.SketchSpacing
import com.example.audioplayer.ui.sketch.SketchSwitchRow
import com.example.audioplayer.ui.sketch.SketchTopBar
import com.example.audioplayer.ui.sketch.SketchTrackRow
import com.example.audioplayer.ui.sketch.SketchTrackUiModel
import java.time.DayOfWeek

/**
 * 新建/编辑定时。所有业务字段、远端文件选择器和精确闹钟权限保持原逻辑。
 */
@Composable
fun TimerEditorScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: TimerEditorViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val connections by viewModel.connections.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
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
                SketchTopBar(
                    title = if (state.id == 0L) "新建定时" else "编辑定时",
                    onBack = onBack,
                    actions = {
                        SketchIconAction(
                            icon = Icons.Default.Check,
                            contentDescription = "保存定时",
                            onClick = viewModel::save,
                        )
                    },
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(SketchSpacing.Page),
                    verticalArrangement = Arrangement.spacedBy(SketchSpacing.Md),
                ) {
                    SketchEditableField(
                        label = "定时名称",
                        value = state.name,
                        onValueChange = viewModel::updateName,
                    )
                    SketchSegmentedTabs(
                        titles = listOf("开始播放", "停止播放"),
                        selectedIndex = if (state.action == TimerAction.START) 0 else 1,
                        onSelected = { index ->
                            viewModel.updateAction(
                                if (index == 0) TimerAction.START else TimerAction.STOP,
                            )
                        },
                    )
                    SketchField(
                        label = "时间",
                        value = "%02d:%02d".format(state.hour, state.minute),
                        trailingArrow = true,
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute -> viewModel.updateTime(hour, minute) },
                                state.hour,
                                state.minute,
                                true,
                            ).show()
                        },
                    )
                    SketchRepeatDays(
                        selectedDays = state.repeatDays,
                        onToggle = viewModel::toggleRepeatDay,
                    )

                    if (state.action == TimerAction.START) {
                        SketchSegmentedTabs(
                            titles = TimerSourceType.entries.map { source ->
                                when (source) {
                                    TimerSourceType.LOCAL_FOLDER -> "本地"
                                    TimerSourceType.SMB_FOLDER -> "SMB"
                                    TimerSourceType.WEBDAV_FOLDER -> "WebDAV"
                                }
                            },
                            selectedIndex = TimerSourceType.entries.indexOf(state.sourceType),
                            onSelected = { index ->
                                viewModel.updateSourceType(TimerSourceType.entries[index])
                            },
                        )
                        SketchSegmentedTabs(
                            titles = listOf("文件夹", "选择文件"),
                            selectedIndex = if (
                                state.selectionType == TimerSelectionType.FOLDER
                            ) {
                                0
                            } else {
                                1
                            },
                            onSelected = { index ->
                                viewModel.updateSelectionType(
                                    if (index == 0) {
                                        TimerSelectionType.FOLDER
                                    } else {
                                        TimerSelectionType.FILES
                                    },
                                )
                            },
                        )

                        if (state.sourceType == TimerSourceType.LOCAL_FOLDER) {
                            SketchLocalTimerSource(
                                state = state,
                                onToggleFile = viewModel::toggleLocalFile,
                                onFolderSelected = viewModel::updateSourcePath,
                            )
                        } else {
                            SketchRemoteTimerSource(
                                state = state,
                                connections = connections,
                                onConnectionSelected = viewModel::updateConnectionId,
                                onSourcePathChange = viewModel::updateSourcePath,
                                onOpenPicker = viewModel::openRemotePicker,
                            )
                        }
                    }

                    SketchSwitchRow(
                        title = "启用定时",
                        subtitle = "保存后按照重复周期自动执行",
                        checked = state.enabled,
                        onCheckedChange = viewModel::updateEnabled,
                    )

                    if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        !context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
                    ) {
                        SketchSecondaryButton(
                            label = "开启精确闹钟权限",
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    ).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    },
                                )
                            },
                        )
                    }

                    SketchPrimaryButton(
                        label = if (state.isSaving) "保存中…" else "保存定时",
                        onClick = viewModel::save,
                    )
                }
            }
        }
    }

    if (state.pickerVisible) {
        AlertDialog(
            onDismissRequest = viewModel::closePicker,
            title = { Text(state.pickerPath) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(SketchSpacing.Xs)) {
                    if (state.pickerPath != "/") {
                        TextButton(onClick = viewModel::openPickerParent) {
                            Text("返回上级")
                        }
                    }
                    when {
                        state.pickerLoading -> CircularProgressIndicator()
                        state.pickerError != null -> Text(state.pickerError.orEmpty())
                        state.pickerEntries.isEmpty() -> {
                            SketchEmptyState(
                                title = "文件夹为空",
                                description = "当前目录没有可选择的音乐。",
                                actionLabel = "重新读取",
                                onAction = { viewModel.closePicker() },
                            )
                        }

                        else -> {
                            LazyColumn {
                                items(state.pickerEntries, key = { it.path }) { entry ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (entry.isDirectory) {
                                                    viewModel.openPickerDirectory(entry)
                                                } else {
                                                    viewModel.togglePickerFile(entry)
                                                }
                                            }
                                            .padding(vertical = SketchSpacing.Sm),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = if (entry.isDirectory) {
                                                "文件夹 · ${entry.name}"
                                            } else if (entry.path in state.selectedFiles) {
                                                "已选择 · ${entry.name}"
                                            } else {
                                                entry.name
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (state.selectionType == TimerSelectionType.FOLDER) {
                    TextButton(onClick = viewModel::choosePickerFolder) {
                        Text("选择当前文件夹")
                    }
                } else {
                    TextButton(onClick = viewModel::confirmPickerFiles) {
                        Text("完成")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closePicker) { Text("取消") }
            },
        )
    }
}

@Composable
private fun SketchRepeatDays(
    selectedDays: Set<DayOfWeek>,
    onToggle: (DayOfWeek) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SketchSpacing.Sm)) {
        DayOfWeek.entries.chunked(5).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Sm),
            ) {
                row.forEach { day ->
                    SketchPill(
                        label = when (day.value) {
                            1 -> "一"
                            2 -> "二"
                            3 -> "三"
                            4 -> "四"
                            5 -> "五"
                            6 -> "六"
                            else -> "日"
                        },
                        selected = day in selectedDays,
                        onClick = { onToggle(day) },
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SketchLocalTimerSource(
    state: TimerEditorUiState,
    onToggleFile: (String) -> Unit,
    onFolderSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SketchSpacing.Sm)) {
        if (state.selectionType == TimerSelectionType.FOLDER) {
            if (state.localFolders.isEmpty()) {
                SketchSettingRow(
                    title = "没有可选本地文件夹",
                    subtitle = "请先授权并扫描音乐",
                )
            } else {
                state.localFolders.forEach { folder ->
                    SketchSettingRow(
                        title = folder.name,
                        subtitle = "${folder.tracks.size} 首歌曲",
                        trailingText = if (state.sourcePath == folder.path) "已选择" else "选择",
                        onClick = { onFolderSelected(folder.path) },
                    )
                }
            }
        } else if (state.localTracks.isEmpty()) {
            SketchSettingRow(
                title = "没有可选本地音乐",
                subtitle = "请先授权并扫描音乐",
            )
        } else {
            state.localTracks.take(100).forEach { track ->
                SketchTrackRow(
                    track = SketchTrackUiModel(
                        id = track.uri,
                        title = track.title,
                        artist = track.artist ?: "未知歌手",
                        album = track.album.orEmpty(),
                        duration = "",
                        selected = track.uri in state.selectedFiles,
                    ),
                    onPlay = { onToggleFile(track.uri) },
                    showDuration = false,
                    showSelection = true,
                )
            }
        }
    }
}

@Composable
private fun SketchRemoteTimerSource(
    state: TimerEditorUiState,
    connections: List<com.example.audioplayer.core.database.ConnectionEntity>,
    onConnectionSelected: (String?) -> Unit,
    onSourcePathChange: (String) -> Unit,
    onOpenPicker: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SketchSpacing.Sm)) {
        if (connections.isEmpty()) {
            SketchSettingRow(
                title = "请先添加 NAS 连接",
                subtitle = "连接 SMB 或 WebDAV 后才能选择网络音乐",
            )
        } else {
            connections.forEach { connection ->
                SketchSettingRow(
                    title = connection.name,
                    subtitle = connection.protocol.name,
                    trailingText = if (state.connectionId == connection.id) "已选择" else "选择",
                    onClick = { onConnectionSelected(connection.id) },
                )
            }
        }
        if (state.selectionType == TimerSelectionType.FOLDER) {
            SketchEditableField(
                label = "NAS 音乐文件夹路径",
                value = state.sourcePath,
                onValueChange = onSourcePathChange,
            )
        } else {
            SketchGlassCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("已选择 ${state.selectedFiles.size} 个 NAS 文件")
            }
        }
        SketchSecondaryButton(
            label = if (state.selectionType == TimerSelectionType.FOLDER) {
                "浏览 NAS 文件夹"
            } else {
                "选择 NAS 文件"
            },
            onClick = onOpenPicker,
        )
    }
}
