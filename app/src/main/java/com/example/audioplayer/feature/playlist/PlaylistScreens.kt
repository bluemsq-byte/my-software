package com.example.audioplayer.feature.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.audioplayer.ui.components.GlassCard
import com.example.audioplayer.ui.components.GlassTrackRow
import com.example.audioplayer.ui.components.TrackMenuAction
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.core.model.AudioTrack

@OptIn(ExperimentalMaterial3Api::class)
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
        topBar = {
            TopAppBar(
                title = { Text("播放列表") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "新建播放列表")
            }
        },
    ) { padding ->
        if (playlists.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("还没有播放列表")
                Button(onClick = { showCreateDialog = true }) { Text("新建播放列表") }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(playlists, key = { it.id }) { playlist ->
                    ListItem(
                        headlineContent = { Text(playlist.name) },
                        supportingContent = { Text("${playlist.itemCount} 首歌曲") },
                        modifier = Modifier.clickable { onOpen(playlist.id) },
                    )
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
                Button(onClick = {
                    viewModel.create(name)
                    name = ""
                    showCreateDialog = false
                    onCreate()
                }) { Text("创建") }
            },
            dismissButton = {
                Button(onClick = { showCreateDialog = false }) { Text("取消") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    onBack: () -> Unit,
    onAddNetworkSongs: () -> Unit,
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTracks by remember { mutableStateOf(setOf<AudioTrack>()) }

    Scaffold(
            containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text(state.name.ifBlank { "播放列表" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (state.selectionMode) viewModel.exitSelectionMode() else viewModel.enterSelectionMode()
                        },
                    ) {
                        Text(if (state.selectionMode) "取消选择" else "选择")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.selectionMode) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(onClick = viewModel::selectAll, modifier = Modifier.weight(1f)) {
                        Text("全选")
                    }
                    Button(
                        onClick = viewModel::playSelected,
                        enabled = state.selectedItemIds.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("播放所选（${state.selectedItemIds.size}）")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { viewModel.playAll() },
                    enabled = state.items.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Text("播放全部")
                }
                Button(
                    onClick = viewModel::openAddLocalDialog,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("添加本地")
                }
                Button(
                    onClick = onAddNetworkSongs,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("添加网络")
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(state.items, key = { _, item -> item.id }) { index, item ->
                    if (state.selectionMode) {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { viewModel.toggleSelection(item.id) },
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = item.id in state.selectedItemIds,
                                    onCheckedChange = { viewModel.toggleSelection(item.id) },
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(item.artist ?: item.sourceType.name.lowercase())
                                }
                            }
                        }
                    } else {
                        GlassTrackRow(
                            title = item.title,
                            subtitle = item.artist ?: item.sourceType.name.lowercase(),
                            leadingIcon = Icons.Default.MusicNote,
                            actions = listOf(
                                TrackMenuAction("立即播放", Icons.Default.PlayArrow) {
                                    viewModel.playAt(index)
                                },
                                TrackMenuAction("下一首播放", Icons.Default.PlayArrow) {
                                    viewModel.playNext(item)
                                },
                                TrackMenuAction("加入播放队列", Icons.Default.QueueMusic) {
                                    viewModel.addToQueue(item)
                                },
                                TrackMenuAction("上移", Icons.Default.KeyboardArrowUp) {
                                    viewModel.move(item.id, -1)
                                },
                                TrackMenuAction("下移", Icons.Default.KeyboardArrowDown) {
                                    viewModel.move(item.id, 1)
                                },
                                TrackMenuAction("从列表移除", Icons.Default.Delete) {
                                    viewModel.remove(item.id)
                                },
                            ),
                            onClick = { viewModel.playAt(index) },
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
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedTracks = if (track in selectedTracks) {
                                    selectedTracks - track
                                } else {
                                    selectedTracks + track
                                }
                            },
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
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
                Button(onClick = {
                    viewModel.addLocalTracks(selectedTracks.toList())
                    selectedTracks = emptySet()
                }) { Text("添加") }
            },
            dismissButton = {
                Button(onClick = { viewModel.closeAddLocalDialog() }) { Text("取消") }
            },
        )
    }
}
