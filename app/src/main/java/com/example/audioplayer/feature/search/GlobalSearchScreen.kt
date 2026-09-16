package com.example.audioplayer.feature.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.ui.formatDuration
import com.example.audioplayer.ui.sketch.SketchBaseScreen
import com.example.audioplayer.ui.sketch.SketchDesign
import com.example.audioplayer.ui.sketch.SketchEmptyState
import com.example.audioplayer.ui.sketch.SketchMenuActionUiModel
import com.example.audioplayer.ui.sketch.SketchPlaylistRow
import com.example.audioplayer.ui.sketch.SketchPlaylistUiModel
import com.example.audioplayer.ui.sketch.SketchSearchField
import com.example.audioplayer.ui.sketch.SketchSectionTitle
import com.example.audioplayer.ui.sketch.SketchSpacing
import com.example.audioplayer.ui.sketch.SketchTopBar
import com.example.audioplayer.ui.sketch.SketchTrackRow
import com.example.audioplayer.ui.sketch.SketchTrackUiModel

/**
 * 全局搜索结果页。
 */
@Composable
fun GlobalSearchScreen(
    onBack: () -> Unit,
    onOpenPlaylist: (Long) -> Unit,
    viewModel: GlobalSearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SketchBaseScreen {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                SketchTopBar(
                    title = "搜索",
                    onBack = onBack,
                )
                SketchSearchField(
                    value = state.query,
                    placeholder = "搜索本地、NAS 和播放列表",
                    onValueChange = viewModel::updateQuery,
                    modifier = Modifier.padding(
                        horizontal = SketchSpacing.Page,
                        vertical = SketchSpacing.Sm,
                    ),
                )
                if (state.isSearching) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = SketchDesign.colors.primary)
                    }
                } else if (
                    state.query.isNotBlank() &&
                    state.localTracks.isEmpty() &&
                    state.remoteTracks.isEmpty() &&
                    state.playlists.isEmpty()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        SketchEmptyState(
                            title = "未找到匹配音乐",
                            description = "可以尝试歌曲名、歌手、专辑或文件夹名称。",
                            actionLabel = "重新输入",
                            onAction = {},
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = SketchSpacing.Xl),
                    ) {
                        if (state.localTracks.isNotEmpty()) {
                            item { SearchSectionTitle("本地音乐") }
                            items(state.localTracks, key = { "local-${it.id}" }) { track ->
                                SketchTrackRow(
                                    track = SketchTrackUiModel(
                                        id = track.id,
                                        title = track.title,
                                        artist = track.artist ?: "未知歌手",
                                        album = track.album.orEmpty(),
                                        duration = formatDuration(track.durationMillis),
                                    ),
                                    onPlay = { viewModel.playLocal(track) },
                                    actions = listOf(
                                        SketchMenuActionUiModel("立即播放") {
                                            viewModel.playLocal(track)
                                        },
                                        SketchMenuActionUiModel("下一首播放") {
                                            viewModel.playNext(track)
                                        },
                                        SketchMenuActionUiModel("加入播放队列") {
                                            viewModel.addToQueue(track)
                                        },
                                    ),
                                )
                            }
                        }
                        if (state.remoteTracks.isNotEmpty()) {
                            item { SearchSectionTitle("网络音乐") }
                            items(
                                state.remoteTracks,
                                key = { "remote-${it.connectionId}-${it.entry.path}" },
                            ) { result ->
                                SketchTrackRow(
                                    track = SketchTrackUiModel(
                                        id = result.entry.path,
                                        title = result.entry.name,
                                        artist = result.connectionName,
                                        album = result.entry.path,
                                        duration = "",
                                    ),
                                    onPlay = { viewModel.playRemote(result) },
                                )
                            }
                        }
                        if (state.playlists.isNotEmpty()) {
                            item { SearchSectionTitle("播放列表") }
                            items(state.playlists, key = { "playlist-${it.id}" }) { playlist ->
                                SketchPlaylistRow(
                                    playlist = SketchPlaylistUiModel(
                                        id = playlist.id.toString(),
                                        name = playlist.name,
                                        songCount = playlist.itemCount,
                                        source = "播放列表",
                                    ),
                                    onClick = { onOpenPlaylist(playlist.id) },
                                )
                            }
                        }
                        state.errors.forEach { error ->
                            item(key = error) {
                                SearchSectionTitle(error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSectionTitle(text: String) {
    SketchSectionTitle(
        text = text,
        modifier = Modifier.padding(
            start = SketchSpacing.Page,
            end = SketchSpacing.Page,
            top = SketchSpacing.Md,
            bottom = SketchSpacing.Xs,
        ),
        style = com.example.audioplayer.ui.sketch.SketchTextStyles.RowTitle,
    )
}
