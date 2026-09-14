package com.example.audioplayer.feature.player

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.audioplayer.core.playback.PlaybackController
import com.example.audioplayer.core.playback.PlaybackMode
import com.example.audioplayer.feature.timer.SleepTimerViewModel
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
    val devices by deviceViewModel.devices.collectAsStateWithLifecycle()
    val currentDevice by deviceViewModel.currentDeviceName.collectAsStateWithLifecycle()
    var draggingValue by remember { mutableFloatStateOf(-1f) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showDeviceDialog by remember { mutableStateOf(false) }
    val threshold = with(LocalDensity.current) { 72.dp.toPx() }

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            deviceViewModel.refresh()
            showDeviceDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (state.artworkUri != null) {
            AsyncImage(
                model = state.artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().blur(55.dp),
            )
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background.copy(alpha = 0.88f)))
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("正在播放") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "收起")
                        }
                    },
                )
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("输出设备", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.weight(1f))
                        TextButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) !=
                                    PackageManager.PERMISSION_GRANTED
                                ) {
                                    bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                } else {
                                    deviceViewModel.refresh()
                                    showDeviceDialog = true
                                }
                            },
                        ) {
                            Icon(Icons.Default.Speaker, contentDescription = null)
                            Text(currentDevice)
                        }
                    }
                }

                item {
                    if (state.artworkUri != null) {
                        AsyncImage(
                            model = state.artworkUri,
                            contentDescription = "专辑封面",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(18.dp)),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF5C2E91), Color(0xFF1B1035)))),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("音乐", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = state.title.ifBlank { "尚未播放" },
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = state.artist.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        val duration = state.durationMillis.coerceAtLeast(1L)
                        val sliderValue = if (draggingValue >= 0f) {
                            draggingValue
                        } else {
                            state.positionMillis.toFloat().coerceIn(0f, duration.toFloat())
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
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = {
                                val next = when (state.playbackMode) {
                                    PlaybackMode.SEQUENTIAL -> PlaybackMode.REPEAT_ALL
                                    PlaybackMode.REPEAT_ALL -> PlaybackMode.REPEAT_ONE
                                    PlaybackMode.REPEAT_ONE -> PlaybackMode.SEQUENTIAL
                                }
                                playbackController.setPlaybackMode(next)
                                Toast.makeText(
                                    context,
                                    when (next) {
                                        PlaybackMode.SEQUENTIAL -> "顺序播放"
                                        PlaybackMode.REPEAT_ALL -> "列表循环"
                                        PlaybackMode.REPEAT_ONE -> "单曲循环"
                                    },
                                    Toast.LENGTH_SHORT,
                                ).show()
                            },
                        ) {
                            Icon(
                                imageVector = when (state.playbackMode) {
                                    PlaybackMode.SEQUENTIAL -> Icons.Default.ArrowForward
                                    PlaybackMode.REPEAT_ALL -> Icons.Default.Repeat
                                    PlaybackMode.REPEAT_ONE -> Icons.Default.RepeatOne
                                },
                                contentDescription = "切换播放模式",
                            )
                        }
                        IconButton(onClick = playbackController::previous, enabled = state.hasPrevious) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "上一首")
                        }
                        FilledIconButton(onClick = playbackController::playPause, modifier = Modifier.size(72.dp)) {
                            Icon(
                                imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (state.isPlaying) "暂停" else "播放",
                                modifier = Modifier.size(40.dp),
                            )
                        }
                        IconButton(onClick = playbackController::next, enabled = state.hasNext) {
                            Icon(Icons.Default.SkipNext, contentDescription = "下一首")
                        }
                        IconButton(onClick = { showSleepTimerDialog = true }) {
                            Icon(Icons.Default.Timer, contentDescription = "倒计时停止")
                        }
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                    Text(
                        text = "接下来播放",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    )
                }

                itemsIndexed(state.queue, key = { _, item -> item.mediaId }) { index, item ->
                    var dragOffset by remember(item.mediaId) { mutableFloatStateOf(0f) }
                    ListItem(
                        headlineContent = { Text(item.title) },
                        supportingContent = { Text(item.artist ?: if (item.isCurrent) "正在播放" else "接下来播放") },
                        trailingContent = {
                            IconButton(onClick = { playbackController.removeQueueItem(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "从队列移除")
                            }
                        },
                        colors = androidx.compose.material3.ListItemDefaults.colors(
                            containerColor = if (item.isCurrent) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            } else {
                                Color.Transparent
                            },
                        ),
                        modifier = Modifier
                            .graphicsLayer { translationY = dragOffset }
                            .clickable {
                                playbackController.seekToQueueItem(index)
                            }
                            .pointerInput(index) {
                                detectDragGesturesAfterLongPress(
                                    onDrag = { change, amount ->
                                        change.consume()
                                        dragOffset += amount.y
                                        when {
                                            dragOffset > threshold && index < state.queue.lastIndex -> {
                                                playbackController.moveQueueItem(index, index + 1)
                                                dragOffset = 0f
                                            }
                                            dragOffset < -threshold && index > 0 -> {
                                                playbackController.moveQueueItem(index, index - 1)
                                                dragOffset = 0f
                                            }
                                        }
                                    },
                                    onDragEnd = { dragOffset = 0f },
                                    onDragCancel = { dragOffset = 0f },
                                )
                            },
                    )
                }

                remainingMillis?.let { remaining ->
                    item {
                        Text(
                            text = "倒计时停止：${remaining / 60_000 + 1} 分钟",
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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
                                Text((if (device.isSelected) "✓ " else "") + device.name)
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
                        ) { Text("$minutes 分钟后停止") }
                    }
                    TextButton(
                        onClick = {
                            sleepTimerViewModel.cancel()
                            showSleepTimerDialog = false
                        },
                    ) { Text("取消倒计时") }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) { Text("关闭") }
            },
        )
    }
}