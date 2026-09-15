package com.example.audioplayer.feature.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.ui.sketch.SketchBaseScreen
import com.example.audioplayer.ui.sketch.SketchEmptyState
import com.example.audioplayer.ui.sketch.SketchIconAction
import com.example.audioplayer.ui.sketch.SketchMenuActionUiModel
import com.example.audioplayer.ui.sketch.SketchPill
import com.example.audioplayer.ui.sketch.SketchPlaylistRow
import com.example.audioplayer.ui.sketch.SketchPlaylistUiModel
import com.example.audioplayer.ui.sketch.SketchSpacing
import com.example.audioplayer.ui.sketch.SketchToolbar
import com.example.audioplayer.ui.sketch.SketchTopBar
import com.example.audioplayer.ui.sketch.SketchTrackRow
import com.example.audioplayer.ui.sketch.SketchTrackUiModel

/**
 * 播放列表列表页。
 */
@Composable
fun PlaylistListScreen(
    onCreate: () -> Unit,
    onOpen: (Long) -> Unit,
    viewModel: PlaylistListViewModel = hiltViewModel(),
) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SketchTopBar(
                title = "播放列表",
                actions = {
                    SketchIconAction(
                        icon = Icons.Default.Add,
                        contentDescription = "新建播放列表",
                        onClick = { showCreateDialog = true },
                    )
                },
            )
            if (playlists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    SketchEmptyState(
                        title = "还没有播放列表",
                        description = "可以混合本地与 NAS 音乐。",
                        actionLabel = "新建播放列表",
                        onAction = { showCreateDialog = true },
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = SketchSpacing.Sm),
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        SketchPlaylistRow(
                            playlist = SketchPlaylistUiModel(
                                id = playlist.id.toString(),
                                name = playlist.name,
                                songCount = playlist.itemCount,
                                source = "本地与 NAS",
                            ),
                            onClick = { onOpen(playlist.id) },
                            actions = listOf(
                                SketchMenuActionUiModel("删除播放列表") {
                                    viewModel.delete(playlist.id)
                                },
                            ),
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("新建播放列表") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.create(name)
                        name = ""
                        showCreateDialog = false
                        onCreate()
                    },
                ) { Text("创建") }
            },
            dismissButton = {
                Button(onClick = { showCreateDialog = false }) { Text("取消") }
            },
        )
    }
}

/**
 * 播放列表详情和多选播放。
 */
@Composable
fun PlaylistDetailScreen(
    onBack: () -> Unit,
    onAddNetworkSongs: () -> Unit,
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTracks by remember { mutableStateOf(setOf<AudioTrack>()) }

    SketchBaseScreen {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                SketchTopBar(
                    title = state.name.ifBlank { "播放列表" },
                    onBack = onBack,
                    actions = {
                        SketchPill(
                            label = if (state.selectionMode) "取消" else "选择",
                            selected = state.selectionMode,
                            onClick = {
                                if (state.selectionMode) {
                                    viewModel.exitSelectionMode()
                                } else {
                                    viewModel.enterSelectionMode()
                                }
                            },
                        )
                    },
                )
                SketchToolbar {
                    SketchPill(
                        label = "播放全部",
                        selected = true,
                        onClick = { viewModel.playAll() },
                    )
                    SketchPill(
                        label = "添加本地",
                        onClick = viewModel::openAddLocalDialog,
                    )
                    SketchPill(
                        label = "添加网络",
                        onClick = onAddNetworkSongs,
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = SketchSpacing.Sm),
                ) {
                    itemsIndexed(
                        state.items,
                        key = { _, item -> item.id },
                    ) { index, item ->
                        SketchTrackRow(
                            track = SketchTrackUiModel(
                                id = item.id.toString(),
                                title = item.title,
                                artist = item.artist ?: item.sourceType.name.lowercase(),
                                album = item.sourceType.name,
                                duration = "",
                                selected = item.id in state.selectedItemIds,
                            ),
                            onPlay = {
                                if (state.selectionMode) {
                                    viewModel.toggleSelection(item.id)
                                } else {
                                    viewModel.playAt(index)
                                }
                            },
                            showDuration = false,
                            showSelection = state.selectionMode,
                            actions = listOf(
                                SketchMenuActionUiModel("立即播放") {
                                    viewModel.playAt(index)
                                },
                                SketchMenuActionUiModel("下一首播放") {
                                    viewModel.playNext(item)
                                },
                                SketchMenuActionUiModel("加入播放队列") {
                                    viewModel.addToQueue(item)
                                },
                                SketchMenuActionUiModel("上移") {
                                    viewModel.move(item.id, -1)
                                },
                                SketchMenuActionUiModel("下移") {
                                    viewModel.move(item.id, 1)
                                },
                                SketchMenuActionUiModel("从列表移除") {
                                    viewModel.remove(item.id)
                                },
                            ),
                        )
                    }
                }
                if (state.selectionMode) {
                    SketchToolbar {
                        SketchPill(
                            label = "全选",
                            onClick = viewModel::selectAll,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        SketchPill(
                            label = "播放所选（${state.selectedItemIds.size}）",
                            selected = state.selectedItemIds.isNotEmpty(),
                            onClick = viewModel::playSelected,
                        )
                    }
                }
            }
        }
    }

    if (state.showAddLocalDialog) {
        AlertDialog(
            onDismissRequest = viewModel::closeAddLocalDialog,
            title = { Text("添加本地歌曲") },
            text = {
                LazyColumn {
                    items(state.localTracks, key = { it.id }) { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTracks = if (track in selectedTracks) {
                                        selectedTracks - track
                                    } else {
                                        selectedTracks + track
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = track in selectedTracks,
                                onCheckedChange = {
                                    selectedTracks = if (track in selectedTracks) {
                                        selectedTracks - track
                                    } else {
                                        selectedTracks + track
                                    }
                                },
                            )
                            Text(track.title, maxLines = 1)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addLocalTracks(selectedTracks.toList())
                        selectedTracks = emptySet()
                    },
                ) { Text("添加") }
            },
            dismissButton = {
                Button(onClick = { viewModel.closeAddLocalDialog() }) { Text("取消") }
            },
        )
    }
}
