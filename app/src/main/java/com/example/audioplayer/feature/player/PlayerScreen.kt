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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.audioplayer.core.playback.PlaybackController
import com.example.audioplayer.core.playback.PlaybackDeviceType
import com.example.audioplayer.core.playback.PlaybackMode
import com.example.audioplayer.feature.timer.SleepTimerViewModel
import com.example.audioplayer.ui.formatDuration
import com.example.audioplayer.ui.sketch.SketchArtworkPlaceholder
import com.example.audioplayer.ui.sketch.SketchBaseScreen
import com.example.audioplayer.ui.sketch.SketchDesign
import com.example.audioplayer.ui.sketch.SketchDeviceBar
import com.example.audioplayer.ui.sketch.SketchDeviceKind
import com.example.audioplayer.ui.sketch.SketchDeviceRow
import com.example.audioplayer.ui.sketch.SketchDeviceUiModel
import com.example.audioplayer.ui.sketch.SketchIconAction
import com.example.audioplayer.ui.sketch.SketchMenuActionUiModel
import com.example.audioplayer.ui.sketch.SketchPlayerControls
import com.example.audioplayer.ui.sketch.SketchQueueRow
import com.example.audioplayer.ui.sketch.SketchRadius
import com.example.audioplayer.ui.sketch.SketchSizes
import com.example.audioplayer.ui.sketch.SketchSpacing
import com.example.audioplayer.ui.sketch.SketchTextStyles
import com.example.audioplayer.ui.sketch.SketchTopBar
import com.example.audioplayer.ui.sketch.SketchTrackUiModel

/**
 * 播放页。保留原 Media3 控制、拖动队列、设备切换和睡眠定时逻辑，
 * 页面结构和视觉切换为 DS Audio 风格草图。
 */
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

    SketchBaseScreen(
        darkTheme = true,
        useMaterialTheme = true,
        forceTheme = true,
    ) {
        if (!state.artworkUri.isNullOrBlank()) {
            AsyncImage(
                model = state.artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(55.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SketchDesign.colors.pageBackground.copy(alpha = 0.88f)),
            )
        }

        Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                SketchTopBar(
                    title = "正在播放",
                    onBack = onBack,
                    navigationIcon = Icons.Default.KeyboardArrowDown,
                    navigationContentDescription = "收起播放页",
                    actions = {
                        SketchIconAction(
                            icon = Icons.Default.Timer,
                            contentDescription = "倒计时停止",
                            onClick = { showSleepTimerDialog = true },
                        )
                    },
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(SketchSpacing.Md),
                ) {
                    item {
                        SketchDeviceBar(
                            deviceName = currentDevice,
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.BLUETOOTH_CONNECT,
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    bluetoothPermissionLauncher.launch(
                                        Manifest.permission.BLUETOOTH_CONNECT,
                                    )
                                } else {
                                    deviceViewModel.refresh()
                                    showDeviceDialog = true
                                }
                            },
                        )
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = SketchSpacing.Xl),
                            contentAlignment = Alignment.Center,
                        ) {
                            val artworkModifier = Modifier
                                .widthIn(max = SketchSizes.ArtworkMax)
                                .fillMaxWidth()
                                .aspectRatio(1f)
                            Box(modifier = artworkModifier) {
                                SketchArtworkPlaceholder(modifier = Modifier.fillMaxSize())
                                if (!state.artworkUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = state.artworkUri,
                                    contentDescription = "专辑封面",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(
                                        RoundedCornerShape(SketchRadius.Album),
                                        ),
                                )
                                }
                            }
                        }
                    }
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = SketchSpacing.Page),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = state.title.ifBlank { "尚未播放" },
                                color = SketchDesign.colors.ink,
                                style = SketchTextStyles.SectionTitle,
                            )
                            Text(
                                text = state.artist.orEmpty(),
                                color = SketchDesign.colors.muted,
                                style = SketchTextStyles.RowSubtitle,
                            )
                        }
                    }
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = SketchSpacing.Page),
                        ) {
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
                                colors = SliderDefaults.colors(
                                    thumbColor = SketchDesign.colors.primary,
                                    activeTrackColor = SketchDesign.colors.primary,
                                    inactiveTrackColor = SketchDesign.colors.onDark.copy(
                                        alpha = 0.24f,
                                    ),
                                ),
                            )
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = formatDuration(state.positionMillis),
                                    color = SketchDesign.colors.muted,
                                    style = SketchTextStyles.Auxiliary,
                                )
                                androidx.compose.foundation.layout.Spacer(
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = formatDuration(state.durationMillis),
                                    color = SketchDesign.colors.muted,
                                    style = SketchTextStyles.Auxiliary,
                                )
                            }
                        }
                    }
                    item {
                        SketchPlayerControls(
                            isPlaying = state.isPlaying,
                            playbackModeIcon = when (state.playbackMode) {
                                PlaybackMode.SEQUENTIAL -> Icons.Default.ArrowForward
                                PlaybackMode.REPEAT_ALL -> Icons.Default.Repeat
                                PlaybackMode.REPEAT_ONE -> Icons.Default.RepeatOne
                            },
                            onPlayPause = playbackController::playPause,
                            onPrevious = playbackController::previous,
                            onNext = playbackController::next,
                            onPlaybackMode = {
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
                            onDevices = {
                                deviceViewModel.refresh()
                                showDeviceDialog = true
                            },
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = SketchSpacing.Page),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "接下来播放",
                                modifier = Modifier.weight(1f),
                                color = SketchDesign.colors.ink,
                                style = SketchTextStyles.RowTitle,
                            )
                            Text(
                                text = "队列 ${state.queue.size} 首",
                                color = SketchDesign.colors.muted,
                                style = SketchTextStyles.Auxiliary,
                            )
                        }
                    }
                    itemsIndexed(
                        items = state.queue,
                        key = { _, item -> item.mediaId },
                    ) { index, item ->
                        var dragOffset by remember(item.mediaId) { mutableFloatStateOf(0f) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { translationY = dragOffset }
                                .pointerInput(index) {
                                    detectDragGesturesAfterLongPress(
                                        onDrag = { change, amount ->
                                            change.consume()
                                            dragOffset += amount.y
                                            when {
                                                dragOffset > threshold &&
                                                    index < state.queue.lastIndex -> {
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
                        ) {
                            SketchQueueRow(
                                indexText = if (item.isCurrent) "▶" else "${index + 1}",
                                track = SketchTrackUiModel(
                                    id = item.mediaId,
                                    title = item.title,
                                    artist = item.artist ?: "未知歌手",
                                    album = "",
                                    duration = "",
                                    playing = item.isCurrent,
                                ),
                                active = item.isCurrent,
                                onClick = { playbackController.seekToQueueItem(index) },
                                actions = listOf(
                                    SketchMenuActionUiModel("立即播放") {
                                        playbackController.seekToQueueItem(index)
                                    },
                                    SketchMenuActionUiModel(
                                        label = "上移",
                                        enabled = index > 0,
                                    ) {
                                        playbackController.moveQueueItem(index, index - 1)
                                    },
                                    SketchMenuActionUiModel(
                                        label = "下移",
                                        enabled = index < state.queue.lastIndex,
                                    ) {
                                        playbackController.moveQueueItem(index, index + 1)
                                    },
                                    SketchMenuActionUiModel("从队列移除") {
                                        playbackController.removeQueueItem(index)
                                    },
                                ),
                            )
                        }
                    }
                    remainingMillis?.let { remaining ->
                        item {
                            Text(
                                text = "倒计时停止：${remaining / 60_000 + 1} 分钟",
                                modifier = Modifier.padding(horizontal = SketchSpacing.Page),
                                color = SketchDesign.colors.muted,
                                style = SketchTextStyles.Auxiliary,
                            )
                        }
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
                Column(verticalArrangement = Arrangement.spacedBy(SketchSpacing.Sm)) {
                    if (devices.isEmpty()) {
                        Text("未发现可用设备，请确认蓝牙或投屏设备已开启")
                    } else {
                        devices.forEach { device ->
                            SketchDeviceRow(
                                device = SketchDeviceUiModel(
                                    id = device.id,
                                    name = device.name,
                                    description = when (device.type) {
                                        PlaybackDeviceType.BLUETOOTH -> "蓝牙音响"
                                        PlaybackDeviceType.CAST -> "Chromecast / Google Cast"
                                        PlaybackDeviceType.SYSTEM -> "Android 媒体路由"
                                    },
                                    kind = when (device.type) {
                                        PlaybackDeviceType.BLUETOOTH -> SketchDeviceKind.BLUETOOTH
                                        PlaybackDeviceType.CAST -> SketchDeviceKind.CAST
                                        PlaybackDeviceType.SYSTEM -> SketchDeviceKind.MEDIA_ROUTE
                                    },
                                    selected = device.isSelected,
                                ),
                                onClick = {
                                    deviceViewModel.select(device.id)
                                    showDeviceDialog = false
                                },
                            )
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
                Column(verticalArrangement = Arrangement.spacedBy(SketchSpacing.Sm)) {
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
