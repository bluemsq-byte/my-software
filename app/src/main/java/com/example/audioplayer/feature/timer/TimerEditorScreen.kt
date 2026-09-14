package com.example.audioplayer.feature.timer

import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.core.model.TimerAction
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
        topBar = {
            TopAppBar(
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                if (state.sourceType == TimerSourceType.LOCAL_FOLDER) {
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
                    OutlinedTextField(
                        value = state.sourcePath,
                        onValueChange = viewModel::updateSourcePath,
                        label = { Text("NAS 音乐文件夹路径") },
                        modifier = Modifier.fillMaxWidth(),
                    )
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
}