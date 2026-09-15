package com.example.audioplayer.feature.timer

import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.core.model.TimerAction
import com.example.audioplayer.core.model.TimerSelectionType
import com.example.audioplayer.core.model.TimerSourceType
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
            containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text(if (state.id == 0L) "新建定时" else "编辑定时") },
                navigationIcon = {
                    OutlinedButton(onClick = onBack) { Text("返回") }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::updateName,
                label = { Text("定时名称") },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = state.action == TimerAction.START,
                    onClick = { viewModel.updateAction(TimerAction.START) },
                    label = { Text("开始播放") },
                )
                FilterChip(
                    selected = state.action == TimerAction.STOP,
                    onClick = { viewModel.updateAction(TimerAction.STOP) },
                    label = { Text("停止播放") },
                )
            }

            OutlinedButton(
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, hour, minute -> viewModel.updateTime(hour, minute) },
                        state.hour,
                        state.minute,
                        true,
                    ).show()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("时间：%02d:%02d".format(state.hour, state.minute))
            }

            Text("重复")
            FlowRow(
                maxItemsInEachRow = 4,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                DayOfWeek.entries.forEach { day ->
                    FilterChip(
                        selected = day in state.repeatDays,
                        onClick = { viewModel.toggleRepeatDay(day) },
                        label = {
                            Text(
                                when (day.value) {
                                    1 -> "一"
                                    2 -> "二"
                                    3 -> "三"
                                    4 -> "四"
                                    5 -> "五"
                                    6 -> "六"
                                    else -> "日"
                                },
                            )
                        },
                    )
                }
            }

            if (state.action == TimerAction.START) {
                Text("音乐来源")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimerSourceType.entries.forEach { source ->
                        FilterChip(
                            selected = state.sourceType == source,
                            onClick = { viewModel.updateSourceType(source) },
                            label = {
                                Text(
                                    when (source) {
                                        TimerSourceType.LOCAL_FOLDER -> "本地"
                                        TimerSourceType.SMB_FOLDER -> "SMB"
                                        TimerSourceType.WEBDAV_FOLDER -> "WebDAV"
                                    },
                                )
                            },
                        )
                    }
                }

                Text("选择方式")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.selectionType == TimerSelectionType.FOLDER,
                        onClick = { viewModel.updateSelectionType(TimerSelectionType.FOLDER) },
                        label = { Text("文件夹") },
                    )
                    FilterChip(
                        selected = state.selectionType == TimerSelectionType.FILES,
                        onClick = { viewModel.updateSelectionType(TimerSelectionType.FILES) },
                        label = { Text("选择文件") },
                    )
                }

                if (state.sourceType == TimerSourceType.LOCAL_FOLDER) {
                    if (state.selectionType == TimerSelectionType.FOLDER) {
                        if (state.localFolders.isEmpty()) {
                            Text("没有可选本地文件夹，请先授权并扫描音乐")
                        } else {
                            state.localFolders.forEach { folder ->
                                FilterChip(
                                    selected = state.sourcePath == folder.path,
                                    onClick = { viewModel.updateSourcePath(folder.path) },
                                    label = { Text("${folder.name} (${folder.tracks.size})") },
                                )
                            }
                        }
                    } else {
                        Text("已选择 ${state.selectedFiles.size} 个本地文件")
                        if (state.localTracks.isEmpty()) {
                            Text("没有可选本地音乐，请先授权并扫描音乐")
                        } else {
                            state.localTracks.take(100).forEach { track ->
                                androidx.compose.foundation.layout.Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleLocalFile(track.uri) },
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = track.uri in state.selectedFiles,
                                        onCheckedChange = { viewModel.toggleLocalFile(track.uri) },
                                    )
                                    Text(track.title, maxLines = 1)
                                }
                            }
                        }
                    }
                } else {
                    Text("NAS 连接")
                    if (connections.isEmpty()) {
                        Text("请先到首页添加 NAS 连接")
                    } else {
                        connections.forEach { connection ->
                            FilterChip(
                                selected = state.connectionId == connection.id,
                                onClick = { viewModel.updateConnectionId(connection.id) },
                                label = { Text(connection.name) },
                            )
                        }
                    }
                    if (state.selectionType == TimerSelectionType.FOLDER) {
                        OutlinedTextField(
                            value = state.sourcePath,
                            onValueChange = viewModel::updateSourcePath,
                            label = { Text("NAS 音乐文件夹路径") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedButton(onClick = viewModel::openRemotePicker) {
                            Text("浏览 NAS 文件夹")
                        }
                    } else {
                        Text("已选择 ${state.selectedFiles.size} 个 NAS 文件")
                        OutlinedButton(onClick = viewModel::openRemotePicker) {
                            Text("选择 NAS 文件")
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Switch(checked = state.enabled, onCheckedChange = viewModel::updateEnabled)
                Text("启用定时")
            }

            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
            ) {
                OutlinedButton(
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:${context.packageName}")
                            },
                        )
                    },
                ) {
                    Text("开启精确闹钟权限")
                }
            }

            Button(
                onClick = viewModel::save,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.isSaving) "保存中…" else "保存定时")
            }
        }
    }

    if (state.pickerVisible) {
        AlertDialog(
            onDismissRequest = viewModel::closePicker,
            title = { Text(state.pickerPath) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (state.pickerPath != "/") {
                        TextButton(onClick = viewModel::openPickerParent) {
                            Text("返回上级")
                        }
                    }
                    if (state.pickerLoading) {
                        CircularProgressIndicator()
                    } else if (state.pickerError != null) {
                        Text(state.pickerError.orEmpty())
                    } else {
                        LazyColumn {
                            items(state.pickerEntries, key = { it.path }) { entry ->
                                androidx.compose.foundation.layout.Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (entry.isDirectory) {
                                                viewModel.openPickerDirectory(entry)
                                            } else {
                                                viewModel.togglePickerFile(entry)
                                            }
                                        },
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                ) {
                                    if (!entry.isDirectory) {
                                        Checkbox(
                                            checked = entry.path in state.selectedFiles,
                                            onCheckedChange = { viewModel.togglePickerFile(entry) },
                                        )
                                    }
                                    Text(if (entry.isDirectory) "📁 ${entry.name}" else entry.name)
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
