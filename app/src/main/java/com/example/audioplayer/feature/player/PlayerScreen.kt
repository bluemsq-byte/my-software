package com.example.audioplayer.feature.player

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.delay

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
    var draggingValue by remember { mutableFloatStateOf(-1f) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showDeviceDialog by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    var vinylRotation by remember { mutableFloatStateOf(0f) }

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            deviceViewModel.refresh()
            showDeviceDialog = true
        }
    }

    LaunchedEffect(state.isPlaying) {
        if (state.isPlaying) {
            while (true) {
                delay(16)
                vinylRotation = (vinylRotation + 0.12f) % 360f
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (state.artworkUri != null) {
            AsyncImage(
                model = state.artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().blur(42.dp),
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color(0xFF5C2E91), Color(0xFF1B1035))),
                ),
            )
        }
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.58f)))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("正在播放", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "收起", tint = Color.White)
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
                            Icon(Icons.Default.Speaker, contentDescription = "选择播放设备", tint = Color.White)
                        }
                        IconButton(onClick = { showSleepTimerDialog = true }) {
                            Icon(Icons.Default.Timer, contentDescription = "倒计时停止", tint = Color.White)
                        }
                    },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(8.dp))
                VinylRecord(
                    artworkUri = state.artworkUri,
                    rotation = if (state.isPlaying) vinylRotation else vinylRotation,
                    onSwipeUp = { if (state.hasNext) playbackController.next() },
                    onSwipeDown = { if (state.hasPrevious) playbackController.previous() },
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = state.title.ifBlank { "尚未播放" },
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = state.artist.orEmpty(),
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(16.dp))
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
                    Text(formatDuration(state.positionMillis), color = Color.White.copy(alpha = 0.65f))
                    Spacer(Modifier.weight(1f))
                    Text(formatDuration(state.durationMillis), color = Color.White.copy(alpha = 0.65f))
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = playbackController::previous, enabled = state.hasPrevious) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "上一首", tint = Color.White)
                    }
                    FilledIconButton(
                        onClick = playbackController::playPause,
                        modifier = Modifier.size(72.dp),
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlaying) "暂停" else "播放",
                            modifier = Modifier.size(40.dp),
                        )
                    }
                    IconButton(onClick = playbackController::next, enabled = state.hasNext) {
                        Icon(Icons.Default.SkipNext, contentDescription = "下一首", tint = Color.White)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            tint = Color.White,
                        )
                    }
                    IconButton(onClick = { showQueue = true }) {
                        Icon(Icons.Default.QueueMusic, contentDescription = "播放队列", tint = Color.White)
                    }
                }
                remainingMillis?.let { remaining ->
                    Text(
                        text = "倒计时停止：${remaining / 60_000 + 1} 分钟",
                        color = Color.White.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }

    if (showQueue) {
        ModalBottomSheet(onDismissRequest = { showQueue = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("正在播放", style = MaterialTheme.typography.titleLarge)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                val threshold = with(LocalDensity.current) { 72.dp.toPx() }
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp)) {
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
                            modifier = Modifier
                                .graphicsLayer { translationY = dragOffset }
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
                                    (if (device.isSelected) "✓ " else "") + device.name +
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

@Composable
private fun VinylRecord(
    artworkUri: String?,
    rotation: Float,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
) {
    var dragDistance by remember { mutableFloatStateOf(0f) }
    Box(
        modifier = Modifier
            .size(290.dp)
            .graphicsLayer { rotationZ = rotation }
            .clip(CircleShape)
            .background(Color(0xFF111111))
            .border(8.dp, Color(0xFF222222), CircleShape)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, amount ->
                        change.consume()
                        dragDistance += amount
                    },
                    onDragEnd = {
                        when {
                            dragDistance < -80f -> onSwipeUp()
                            dragDistance > 80f -> onSwipeDown()
                        }
                        dragDistance = 0f
                    },
                    onDragCancel = { dragDistance = 0f },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .border(2.dp, Color(0xFF333333), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(230.dp)
                .border(2.dp, Color(0xFF292929), CircleShape),
        )
        if (artworkUri != null) {
            AsyncImage(
                model = artworkUri,
                contentDescription = "专辑封面",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(170.dp).clip(CircleShape),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Color(0xFF8E5BD4), Color(0xFF3A1E64)))),
                contentAlignment = Alignment.Center,
            ) {
                Text("音乐", color = Color.White)
            }
        }
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(Color(0xFF111111)),
        )
    }
}