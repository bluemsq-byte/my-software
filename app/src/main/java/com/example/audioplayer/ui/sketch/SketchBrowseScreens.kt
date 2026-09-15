package com.example.audioplayer.ui.sketch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 草图 07：NAS 文件夹浏览。
 */
@Composable
fun Sketch07NasFolderScreen(
    pathTitle: String = "music / 华语",
    folderTitle: String = "1、泰勒斯威夫特",
    songCount: Int = 0,
    tracks: List<SketchTrackUiModel> = emptyList(),
    miniPlayer: SketchMiniPlayerUiModel? = null,
    onBack: () -> Unit = {},
    onSearch: () -> Unit = {},
    onMore: () -> Unit = {},
    onPlayAll: () -> Unit = {},
    onAddToQueue: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {},
    onTrackClick: (SketchTrackUiModel) -> Unit = {},
    onTrackMore: (SketchTrackUiModel) -> Unit = {},
    onMiniPlayPause: () -> Unit = {},
    onMiniNext: () -> Unit = {},
    onNavSelected: (Int) -> Unit = {},
) {
    SketchBaseScreen {
        SketchMainScaffold(
            selectedNavIndex = 0,
            onNavSelected = onNavSelected,
            miniPlayer = miniPlayer,
            onMiniPlayPause = onMiniPlayPause,
            onMiniNext = onMiniNext,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SketchTopBar(
                    title = pathTitle,
                    onBack = onBack,
                    actions = {
                        SketchIconAction(
                            icon = Icons.Default.Search,
                            contentDescription = "搜索当前文件夹",
                            onClick = onSearch,
                        )
                        SketchIconAction(
                            icon = Icons.Default.MoreVert,
                            contentDescription = "文件夹更多操作",
                            onClick = onMore,
                        )
                    },
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = SketchSpacing.Sm),
                ) {
                    item {
                        SketchFolderHeroCard(
                            title = folderTitle,
                            songCount = songCount,
                            onPlayAll = onPlayAll,
                            onAddToQueue = onAddToQueue,
                            onAddToPlaylist = onAddToPlaylist,
                        )
                    }
                    items(tracks, key = { it.id }) { track ->
                        SketchTrackRow(
                            track = track,
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
 * 草图 08：歌曲三点菜单。
 */
@Composable
fun Sketch08TrackMenuScreen(
    pathTitle: String = "music / 华语",
    folderTitle: String = "1、泰勒斯威夫特",
    songCount: Int = 0,
    track: SketchTrackUiModel,
    actions: List<SketchMenuActionUiModel> = emptyList(),
    onBack: () -> Unit = {},
    onAction: (SketchMenuActionUiModel) -> Unit = {},
    onTrackClick: () -> Unit = {},
) {
    SketchBaseScreen {
        Column(modifier = Modifier.fillMaxSize()) {
            SketchTopBar(
                title = pathTitle,
                onBack = onBack,
            )
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    SketchFolderHeroCard(
                        title = folderTitle,
                        songCount = songCount,
                        compact = true,
                    )
                    SketchTrackRow(
                        track = track,
                        onPlay = onTrackClick,
                    )
                }
                SketchActionMenu(
                    actions = actions,
                    onAction = onAction,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(
                            top = SketchSpacing.Xl,
                            end = SketchSpacing.Page,
                        ),
                )
            }
        }
    }
}

/**
 * 文件夹头部的公共卡片。
 */
@Composable
fun SketchFolderHeroCard(
    title: String,
    songCount: Int,
    compact: Boolean = false,
    onPlayAll: () -> Unit = {},
    onAddToQueue: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {},
) {
    SketchGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = SketchSpacing.Page,
                vertical = SketchSpacing.Xs,
            ),
        contentPadding = PaddingValues(SketchSpacing.Md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Md),
        ) {
            SketchArtworkPlaceholder(
                modifier = Modifier.size(SketchSizes.HeroArtwork),
            )
            Column(modifier = Modifier.weight(1f)) {
                SketchSectionTitle(title)
                Text(
                    text = "$songCount 首歌曲",
                    color = SketchDesign.colors.muted,
                    style = SketchTextStyles.Auxiliary,
                    modifier = Modifier.padding(top = SketchSpacing.Xs),
                )
                if (!compact) {
                    Row(
                        modifier = Modifier.padding(top = SketchSpacing.Sm),
                        horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Sm),
                    ) {
                        SketchIconAction(
                            icon = Icons.Default.Shuffle,
                            contentDescription = "随机播放全部",
                            onClick = onPlayAll,
                        )
                        SketchIconAction(
                            icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                            contentDescription = "加入播放队列",
                            onClick = onAddToQueue,
                        )
                        SketchIconAction(
                            icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                            contentDescription = "加入播放列表",
                            onClick = onAddToPlaylist,
                        )
                    }
                }
            }
            if (compact) {
                Spacer(modifier = Modifier.size(SketchSpacing.Sm))
            }
        }
    }
}
