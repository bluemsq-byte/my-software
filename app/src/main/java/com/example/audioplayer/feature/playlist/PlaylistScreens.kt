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
import com.example.audioplayer.core.database.ConnectionEntity
import com.example.audioplayer.feature.library.LibraryViewModel
import com.example.audioplayer.core.storage.LocalFolderNode
import com.example.audioplayer.core.storage.LocalFolderTree
import com.example.audioplayer.ui.sketch.SketchBaseScreen
import com.example.audioplayer.ui.sketch.SketchConnectionRow
import com.example.audioplayer.ui.sketch.SketchConnectionUiModel
import com.example.audioplayer.ui.sketch.SketchEmptyState
import com.example.audioplayer.ui.sketch.SketchFolderRow
import com.example.audioplayer.ui.sketch.SketchFolderUiModel
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
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.audioplayer.ui.formatDuration

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
    onAddLocalSongs: (Long) -> Unit,
    onAddNetworkSongs: (Long) -> Unit,
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
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
                        onClick = { onAddLocalSongs(viewModel.playlistId) },
                    )
                    SketchPill(
                        label = "添加网络",
                        onClick = { onAddNetworkSongs(viewModel.playlistId) },
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
                            label = "上移",
                            enabled = state.selectedItemIds.isNotEmpty(),
                            onClick = { viewModel.moveSelected(-1) },
                        )
                        SketchPill(
                            label = "下移",
                            enabled = state.selectedItemIds.isNotEmpty(),
                            onClick = { viewModel.moveSelected(1) },
                        )
                        SketchPill(
                            label = "移除",
                            enabled = state.selectedItemIds.isNotEmpty(),
                            onClick = viewModel::removeSelected,
                        )
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

}

/**
 * 从播放列表进入的网络音乐选择入口，先选择 NAS，再进入文件夹多选。
 */
@Composable
fun PlaylistNetworkSourceScreen(
    onBack: () -> Unit,
    onConnectionSelected: (ConnectionEntity) -> Unit,
    onAddConnection: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val connections by viewModel.connections.collectAsStateWithLifecycle()

    SketchBaseScreen {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                SketchTopBar(
                    title = "选择网络音乐",
                    onBack = onBack,
                )
                if (connections.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        SketchEmptyState(
                            title = "还没有网络音乐连接",
                            description = "先添加 SMB 或 WebDAV 连接。",
                            actionLabel = "添加网络音乐",
                            onAction = onAddConnection,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = SketchSpacing.Sm),
                    ) {
                        items(connections, key = ConnectionEntity::id) { connection ->
                            SketchConnectionRow(
                                connection = SketchConnectionUiModel(
                                    id = connection.id,
                                    name = connection.name,
                                    protocol = connection.protocol.name,
                                    status = "选择后浏览并添加歌曲",
                                ),
                                onClick = { onConnectionSelected(connection) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 添加本地歌曲：按真实目录层级进入文件夹，再选择歌曲。
 */
@Composable
fun LocalPlaylistPickerScreen(
    onBack: () -> Unit,
    onAdded: () -> Unit,
    viewModel: LocalPlaylistPickerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val root = state.root
    val current = remember(root, state.currentPath) {
        root?.let { tree ->
            state.currentPath?.let { path -> LocalFolderTree.findNode(tree, path) } ?: tree
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    SketchBaseScreen {
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
                    title = "添加本地歌曲",
                    onBack = onBack,
                )
                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.CircularProgressIndicator()
                        }
                    }

                    current == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            SketchEmptyState(
                                title = "没有找到本地音乐",
                                description = "请先授权并扫描本地音乐。",
                                actionLabel = "返回",
                                onAction = onBack,
                            )
                        }
                    }

                    else -> {
                        if (root != null && current.path != root.path) {
                            SketchToolbar {
                                Text(
                                    text = current.path,
                                    modifier = Modifier.weight(1f),
                                    color = Color.Gray,
                                    maxLines = 1,
                                )
                                SketchPill(
                                    label = "返回上级",
                                    onClick = viewModel::openParent,
                                )
                            }
                        }
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = SketchSpacing.Sm),
                        ) {
                            items(current.childFolders, key = { it.path }) { folder ->
                                SketchFolderRow(
                                    folder = SketchFolderUiModel(
                                        id = folder.path,
                                        name = folder.name,
                                        songCount = folder.descendantTrackCount(),
                                        description = "文件夹",
                                    ),
                                    onClick = { viewModel.openFolder(folder) },
                                )
                            }
                            items(current.tracks, key = { it.id }) { track ->
                                SketchTrackRow(
                                    track = SketchTrackUiModel(
                                        id = track.id,
                                        title = track.title,
                                        artist = track.artist ?: "未知歌手",
                                        album = track.album.orEmpty(),
                                        duration = formatDuration(track.durationMillis),
                                        selected = track.id in state.selectedTrackIds,
                                    ),
                                    onPlay = { viewModel.toggle(track) },
                                    showSelection = true,
                                )
                            }
                        }
                        SketchToolbar {
                            Text(
                                text = "已选择 ${state.selectedTrackIds.size} 首",
                                modifier = Modifier.weight(1f),
                                color = Color.Gray,
                            )
                            SketchPill(
                                label = "添加到播放列表",
                                selected = state.selectedTrackIds.isNotEmpty(),
                                enabled = state.selectedTrackIds.isNotEmpty(),
                                onClick = { viewModel.addSelected(onAdded) },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun LocalFolderNode.descendantTrackCount(): Int =
    tracks.size + childFolders.sumOf { it.descendantTrackCount() }
