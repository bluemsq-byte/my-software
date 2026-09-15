package com.example.audioplayer.ui.sketch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 草图 12：播放页 · DS Audio 风格。
 *
 * 黑胶、模糊背景、上下滑动切歌等动画属于后续交互层，这里先输出静态结构。
 */
@Composable
fun Sketch12PlayerScreen(
    title: String = "",
    artist: String = "",
    album: String = "",
    deviceName: String = "",
    isPlaying: Boolean = false,
    playbackModeIndex: Int = 1,
    queue: List<SketchTrackUiModel> = emptyList(),
    onCollapse: () -> Unit = {},
    onDeviceClick: () -> Unit = {},
    onMore: () -> Unit = {},
    onTimer: () -> Unit = {},
    onPlayPause: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onPlaybackMode: () -> Unit = {},
    onDevices: () -> Unit = {},
    onQueueMore: (SketchTrackUiModel) -> Unit = {},
    onNavSelected: (Int) -> Unit = {},
) {
    SketchBaseScreen(darkTheme = true) {
        SketchMainScaffold(
            selectedNavIndex = -1,
            onNavSelected = onNavSelected,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SketchTopBar(
                    title = "正在播放",
                    onBack = onCollapse,
                    navigationIcon = Icons.Default.KeyboardArrowDown,
                    navigationContentDescription = "收起播放页",
                    actions = {
                        SketchIconAction(
                            icon = Icons.Default.MoreVert,
                            contentDescription = "播放页更多操作",
                            onClick = onMore,
                        )
                        SketchIconAction(
                            icon = Icons.Default.Timer,
                            contentDescription = "定时停止",
                            onClick = onTimer,
                        )
                    },
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            start = SketchSpacing.Page,
                            end = SketchSpacing.Page,
                            top = SketchSpacing.Md,
                            bottom = SketchSpacing.Xl,
                        ),
                    verticalArrangement = Arrangement.spacedBy(SketchSpacing.Md),
                ) {
                    SketchDeviceBar(
                        deviceName = deviceName,
                        onClick = onDeviceClick,
                    )
                    SketchArtworkPlaceholder(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .widthIn(max = SketchSizes.ArtworkMax)
                            .fillMaxWidth()
                            .aspectRatio(SketchDefaults.ArtworkAspectRatio),
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = title,
                            color = SketchDesign.colors.ink,
                            style = SketchTextStyles.SectionTitle,
                        )
                        Text(
                            text = listOf(artist, album)
                                .filter { it.isNotBlank() }
                                .joinToString(" · "),
                            color = SketchDesign.colors.muted,
                            style = SketchTextStyles.RowSubtitle,
                            modifier = Modifier.padding(top = SketchSpacing.Xs),
                        )
                    }
                    SketchProgressBar(progress = SketchDefaults.PlayerProgress)
                    SketchPlayerControls(
                        isPlaying = isPlaying,
                        playbackModeIcon = SketchPlaybackModeIcon(playbackModeIndex),
                        onPlayPause = onPlayPause,
                        onPrevious = onPrevious,
                        onNext = onNext,
                        onPlaybackMode = onPlaybackMode,
                        onDevices = onDevices,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "接下来播放",
                            modifier = Modifier.weight(1f),
                            color = SketchDesign.colors.ink,
                            style = SketchTextStyles.RowTitle,
                        )
                        Text(
                            text = "队列 ${queue.size} 首",
                            color = SketchDesign.colors.muted,
                            style = SketchTextStyles.Auxiliary,
                        )
                    }
                    queue.forEachIndexed { index, track ->
                        SketchQueueRow(
                            indexText = if (track.playing) "▶" else "${index + 1}",
                            track = track,
                            active = track.playing,
                            onMore = { onQueueMore(track) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * 草图 13：播放设备选择。
 */
@Composable
fun Sketch13DevicePickerScreen(
    devices: List<SketchDeviceUiModel> = emptyList(),
    onBack: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onDeviceSelected: (SketchDeviceUiModel) -> Unit = {},
) {
    SketchBaseScreen {
        Column(modifier = Modifier.fillMaxSize()) {
            SketchTopBar(
                title = "选择播放设备",
                onBack = onBack,
            )
            SketchToolbar {
                SketchPill(label = "本机播放", selected = true)
                SketchPill(label = "刷新设备", onClick = onRefresh)
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = SketchSpacing.Sm),
            ) {
                itemsIndexed(devices, key = { _, item -> item.id }) { _, device ->
                    SketchDeviceRow(
                        device = device,
                        onClick = { onDeviceSelected(device) },
                    )
                }
            }
        }
    }
}
