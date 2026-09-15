package com.example.audioplayer.ui.sketch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 草图 09：最近播放。
 */
@Composable
fun Sketch09RecentPlayScreen(
    tracks: List<SketchTrackUiModel> = emptyList(),
    relativeTimes: Map<String, String> = emptyMap(),
    onClear: () -> Unit = {},
    onTrackClick: (SketchTrackUiModel) -> Unit = {},
    onTrackMore: (SketchTrackUiModel) -> Unit = {},
    onNavSelected: (Int) -> Unit = {},
) {
    SketchBaseScreen {
        SketchMainScaffold(
            selectedNavIndex = 1,
            onNavSelected = onNavSelected,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SketchTopBar(
                    title = "最近播放",
                    actions = {
                        SketchIconAction(
                            icon = Icons.Default.DeleteSweep,
                            contentDescription = "清空最近播放",
                            onClick = onClear,
                        )
                    },
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = SketchSpacing.Sm),
                ) {
                    items(tracks, key = { it.id }) { track ->
                        SketchTrackRow(
                            track = track.copy(
                                duration = relativeTimes[track.id].orEmpty(),
                            ),
                            onPlay = { onTrackClick(track) },
                            onMore = { onTrackMore(track) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * 草图 10：播放列表列表。
 */
@Composable
fun Sketch10PlaylistListScreen(
    playlists: List<SketchPlaylistUiModel> = emptyList(),
    onAdd: () -> Unit = {},
    onPlaylistClick: (SketchPlaylistUiModel) -> Unit = {},
    onNavSelected: (Int) -> Unit = {},
) {
    SketchBaseScreen {
        SketchMainScaffold(
            selectedNavIndex = 2,
            onNavSelected = onNavSelected,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SketchTopBar(
                    title = "播放列表",
                    actions = {
                        SketchIconAction(
                            icon = Icons.Default.Add,
                            contentDescription = "新建播放列表",
                            onClick = onAdd,
                        )
                    },
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = SketchSpacing.Sm),
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        SketchPlaylistRow(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * 草图 11：播放列表详情与多选。
 */
@Composable
fun Sketch11PlaylistDetailScreen(
    title: String = "我的收藏",
    tracks: List<SketchTrackUiModel> = emptyList(),
    selectedCount: Int = 0,
    selecting: Boolean = false,
    onBack: () -> Unit = {},
    onToggleSelecting: () -> Unit = {},
    onPlayAll: () -> Unit = {},
    onAddLocal: () -> Unit = {},
    onAddNetwork: () -> Unit = {},
    onPlaySelected: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    onTrackClick: (SketchTrackUiModel) -> Unit = {},
    onTrackMore: (SketchTrackUiModel) -> Unit = {},
) {
    SketchBaseScreen {
        Column(modifier = Modifier.fillMaxSize()) {
            SketchTopBar(
                title = title,
                onBack = onBack,
                actions = {
                    SketchPill(
                        label = if (selecting) "取消" else "选择",
                        selected = selecting,
                        onClick = onToggleSelecting,
                    )
                },
            )
            SketchToolbar {
                SketchPill(
                    label = "播放全部",
                    selected = true,
                    onClick = onPlayAll,
                )
                SketchPill(
                    label = "添加本地",
                    onClick = onAddLocal,
                )
                SketchPill(
                    label = "添加网络",
                    onClick = onAddNetwork,
                )
            }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = SketchSpacing.Sm),
            ) {
                items(tracks, key = { it.id }) { track ->
                    SketchTrackRow(
                        track = track,
                        onPlay = { onTrackClick(track) },
                        onMore = { onTrackMore(track) },
                        showSelection = selecting,
                    )
                }
            }
            if (selecting) {
                SketchToolbar {
                    SketchPill(
                        label = "全选",
                        onClick = onSelectAll,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    SketchPill(
                        label = "播放所选（$selectedCount）",
                        selected = selectedCount > 0,
                        onClick = onPlaySelected,
                    )
                }
                Text(
                    text = "多选后一起播放",
                    color = SketchDesign.colors.muted,
                    style = SketchTextStyles.Auxiliary,
                    modifier = Modifier.padding(
                        horizontal = SketchSpacing.Page,
                        vertical = SketchSpacing.Xs,
                    ),
                )
            }
        }
    }
}
