package com.example.audioplayer.feature.browser

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.core.network.RemoteEntry
import com.example.audioplayer.core.network.RemotePath
import com.example.audioplayer.ui.sketch.SketchBaseScreen
import com.example.audioplayer.ui.sketch.SketchDesign
import com.example.audioplayer.ui.sketch.SketchEmptyState
import com.example.audioplayer.ui.sketch.SketchFolderHeroCard
import com.example.audioplayer.ui.sketch.SketchFolderRow
import com.example.audioplayer.ui.sketch.SketchFolderUiModel
import com.example.audioplayer.ui.sketch.SketchIconAction
import com.example.audioplayer.ui.sketch.SketchMenuActionUiModel
import com.example.audioplayer.ui.sketch.SketchSearchField
import com.example.audioplayer.ui.sketch.SketchSpacing
import com.example.audioplayer.ui.sketch.SketchSwitchRow
import com.example.audioplayer.ui.sketch.SketchTopBar
import com.example.audioplayer.ui.sketch.SketchTrackRow
import com.example.audioplayer.ui.sketch.SketchTrackUiModel

/**
 * NAS / WebDAV 文件夹浏览页。保留原目录、搜索、播放和加入播放列表逻辑。
 */
@Composable
fun BrowserScreen(
    onBack: () -> Unit,
    onOpenPlayer: () -> Unit,
    viewModel: BrowserViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    var pendingPlaylistEntry by remember { mutableStateOf<RemoteEntry?>(null) }
    var searchVisible by remember { mutableStateOf(false) }

    SketchBaseScreen {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                SketchTopBar(
                    title = state.path,
                    onBack = onBack,
                    actions = {
                        SketchIconAction(
                            icon = Icons.Default.Search,
                            contentDescription = "搜索当前文件夹",
                            selected = searchVisible,
                            onClick = { searchVisible = !searchVisible },
                        )
                        SketchIconAction(
                            icon = Icons.Default.Refresh,
                            contentDescription = "刷新",
                            onClick = { viewModel.load() },
                        )
                    },
                )
                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = SketchDesign.colors.primary)
                        }
                    }

                    state.errorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            SketchEmptyState(
                                title = "无法读取当前文件夹",
                                description = state.errorMessage.orEmpty(),
                                actionLabel = "重试",
                                onAction = { viewModel.load() },
                            )
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = SketchSpacing.Sm),
                        ) {
                            SketchFolderHeroCard(
                                title = RemotePath.name(state.path).ifBlank { "音乐文件夹" },
                                songCount = state.entries.count { it.isAudioFile },
                                onPlayAll = viewModel::playFolder,
                            )
                            if (searchVisible) {
                                SketchSearchField(
                                    value = state.searchQuery,
                                    placeholder = "搜索当前文件夹",
                                    onValueChange = viewModel::updateSearchQuery,
                                    modifier = Modifier.padding(
                                        horizontal = SketchSpacing.Page,
                                        vertical = SketchSpacing.Sm,
                                    ),
                                )
                            }
                            SketchSwitchRow(
                                title = "仅显示音频",
                                subtitle = "隐藏当前文件夹中的非音频文件",
                                checked = state.audioOnly,
                                onCheckedChange = { viewModel.toggleAudioOnly() },
                            )
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = SketchSpacing.Sm),
                            ) {
                                RemotePath.parent(state.path)?.let { parent ->
                                    item(key = "parent") {
                                        SketchFolderRow(
                                            folder = SketchFolderUiModel(
                                                id = "parent",
                                                name = "返回上级",
                                                songCount = 0,
                                                description = parent,
                                            ),
                                            onClick = { viewModel.load(parent) },
                                        )
                                    }
                                }
                                items(state.visibleEntries, key = { it.path }) { entry ->
                                    if (entry.isDirectory) {
                                        SketchFolderRow(
                                            folder = SketchFolderUiModel(
                                                id = entry.path,
                                                name = entry.name,
                                                songCount = 0,
                                                description = "文件夹",
                                            ),
                                            onClick = { viewModel.openDirectory(entry) },
                                        )
                                    } else {
                                        SketchTrackRow(
                                            track = SketchTrackUiModel(
                                                id = entry.path,
                                                title = entry.name,
                                                artist = "网络音乐",
                                                album = state.path,
                                                duration = "",
                                            ),
                                            onPlay = {
                                                viewModel.play(entry)
                                                onOpenPlayer()
                                            },
                                            actions = listOf(
                                                SketchMenuActionUiModel("立即播放") {
                                                    viewModel.play(entry)
                                                    onOpenPlayer()
                                                },
                                                SketchMenuActionUiModel("下一首播放") {
                                                    viewModel.playNext(entry)
                                                },
                                                SketchMenuActionUiModel("加入播放队列") {
                                                    viewModel.addToQueue(entry)
                                                },
                                                SketchMenuActionUiModel("加入播放列表") {
                                                    pendingPlaylistEntry = entry
                                                },
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingPlaylistEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingPlaylistEntry = null },
            title = { Text("加入播放列表") },
            text = {
                Column {
                    if (playlists.isEmpty()) {
                        Text("还没有播放列表，请先到播放列表页面创建")
                    } else {
                        playlists.forEach { playlist ->
                            TextButton(
                                onClick = {
                                    viewModel.addToPlaylist(playlist.id, entry)
                                    pendingPlaylistEntry = null
                                },
                            ) {
                                Text(playlist.name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pendingPlaylistEntry = null }) { Text("关闭") }
            },
        )
    }
}
