package com.example.audioplayer.feature.player

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.feature.timer.SleepTimerViewModel
import com.example.audioplayer.core.playback.PlaybackController
import com.example.audioplayer.core.playback.PlaybackMode
import com.example.audioplayer.ui.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    playbackController: PlaybackController,
    onBack: () -> Unit,
    sleepTimerViewModel: SleepTimerViewModel = hiltViewModel(),
    deviceViewModel: PlaybackDeviceViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by playbackController.state.collectAsStateWithLifecycle()
    val remainingMillis by sleepTimerViewModel.remainingMillis.collectAsStateWithLifecycle()
    var draggingValue by remember { mutableFloatStateOf(-1f) }
    val devices by deviceViewModel.devices.collectAsStateWithLifecycle()
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showDeviceDialog by remember { mutableStateOf(false) }
    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            deviceViewModel.refresh()
            showDeviceDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("正在播放") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "收起")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.BLUETOOTH_CONNECT,
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                            } else {
                                deviceViewModel.refresh()
                                showDeviceDialog = true
                            }
                        },
                    ) {
                        Icon(Icons.Default.Speaker, contentDescription = "选择播放设备")
                    }
                    IconButton(onClick = { showSleepTimerDialog = true }) {
                        Icon(Icons.Default.Timer, contentDescription = "倒计时停止")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(24.dp))
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = state.title.ifBlank { "尚未播放" },
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = state.artist ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                val duration = state.durationMillis.coerceAtLeast(1L)
                val sliderValue = when {
                    draggingValue >= 0f -> draggingValue
                    else -> state.positionMillis.toFloat().coerceIn(0f, duration.toFloat())
                }
                Slider(
                    value = sliderValue,
                    onValueChange = { draggingValue = it },
                    onValueChangeFinished = {
                        playbackController.seekTo(draggingValue.toLong())
                        draggingValue = -1f
                    },
                    valueRange = 0f..duration.toFloat(),
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(formatDuration(state.positionMillis))
                    Spacer(Modifier.weight(1f))
                    Text(formatDuration(state.durationMillis))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.playbackMode == PlaybackMode.SEQUENTIAL,
                    onClick = { playbackController.setPlaybackMode(PlaybackMode.SEQUENTIAL) },
                    label = { Text("顺序") },
                )
                FilterChip(
                    selected = state.playbackMode == PlaybackMode.REPEAT_ALL,
                    onClick = { playbackController.setPlaybackMode(PlaybackMode.REPEAT_ALL) },
                    label = { Text("列表循环") },
                )
                FilterChip(
                    selected = state.playbackMode == PlaybackMode.REPEAT_ONE,
                    onClick = { playbackController.setPlaybackMode(PlaybackMode.REPEAT_ONE) },
                    label = { Text("单曲循环") },
                )
            }

            remainingMillis?.let { remaining ->
                Text("倒计时停止：${remaining / 60_000 + 1} 分钟")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = playbackController::previous,
                    enabled = state.hasPrevious,
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "上一首")
                }
                IconButton(onClick = playbackController::playPause) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "暂停" else "播放",
                        modifier = Modifier.size(56.dp),
                    )
                }
                IconButton(
                    onClick = playbackController::next,
                    enabled = state.hasNext,
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "下一首")
                }
            }
        }
    }

    if (showDeviceDialog) {
        AlertDialog(
            onDismissRequest = { showDeviceDialog = false },
            title = { Text("选择播放设备") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (devices.isEmpty()) {
                        Text("未发现可用设备，请确认蓝牙或投屏设备已开启")
                    } else {
                        devices.forEach { device ->
                            TextButton(
                                onClick = {
                                    deviceViewModel.select(device.id)
                                    showDeviceDialog = false
                                },
                            ) {
                                Text(
                                    (if (device.isSelected) "✓ " else "") +
                                        device.name +
                                        when (device.type.name) {
                                            "CAST" -> "（Cast）"
                                            "BLUETOOTH" -> "（蓝牙）"
                                            else -> ""
                                        },
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDeviceDialog = false }) { Text("关闭") }
            },
        )
    }

    if (showSleepTimerDialog) {
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            title = { Text("倒计时停止") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 15, 30, 60).forEach { minutes ->
                        TextButton(
                            onClick = {
                                sleepTimerViewModel.start(minutes)
                                showSleepTimerDialog = false
                            },
                        ) {
                            Text("$minutes 分钟后停止")
                        }
                    }
                    TextButton(
                        onClick = {
                            sleepTimerViewModel.cancel()
                            showSleepTimerDialog = false
                        },
                    ) {
                        Text("取消倒计时")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) {
                    Text("关闭")
                }
            },
        )
    }
}